package me.tomassetti.ast

import org.antlr.runtime.tree.CommonTree
import org.antlr.v4.Tool
import org.antlr.v4.tool.*
import org.antlr.v4.tool.ast.*
import java.util.*

val TOKEN_TYPE = "<String>"

data class Element(val name: String, val type: String, val multiple: Boolean, val toExclude: List<String> = LinkedList<String>()) {
    override fun toString(): String {
        val desc = if (type == TOKEN_TYPE) "Token" else type
        val mult = if (multiple) "*" else ""
        val excl = if (toExclude.isEmpty()) "" else " (excl: $toExclude)"
        return "$name:$desc$mult $excl"
    }
}

fun simpleToken(name: String) = Element(name, TOKEN_TYPE, false)

fun multipleToken(name: String) = Element(name, TOKEN_TYPE, true)

fun simpleChild(name: String, type: String = name) = Element(name, type, false)

fun multipleChild(name: String, type: String = name) = Element(name, type, true)

class Entity(val name: String, val elements: Set<Element>, val superclass: Entity? = null, val isAbstract: Boolean = false) {
    override fun toString(): String{
        return "Entity(name='$name', elements=$elements, superclass=${superclass?.name}, isAbstract=$isAbstract)"
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other !is Entity) return false

        if (name != other.name) return false
        if (elements != other.elements) return false
        if (superclass != other.superclass) return false
        if (isAbstract != other.isAbstract) return false

        return true
    }

    override fun hashCode(): Int{
        var result = name.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + (superclass?.hashCode() ?: 0)
        result = 31 * result + isAbstract.hashCode()
        return result
    }
}

class Metamodel(val entities : List<Entity>) {
    fun byName(name: String) : Entity {
        val e = entities.find { e -> e.name == name }
        if (e == null) {
            throw IllegalArgumentException("Unknown entity $name")
        } else {
            return e
        }
    }
}

class ParseTreeToAstMapper() {

    var debug = false

    private fun debugMsg(msg: String) {
        if (debug) {
            println("DEBUG $msg")
        }
    }

    private fun isMultiple(ast: CommonTree?) : Boolean {
        when (ast?.parent) {
            null -> return false
            is Rule -> return false
            is StarBlockAST -> return true
            is PlusBlockAST -> return true
            else -> return isMultiple(ast?.parent)
        }
    }

    private fun considerAlternative(alt : Alternative) : Set<Element>{
        debugMsg("  Considering alternative")
        val res = HashSet<Element>()
        val labelledTypes : java.util.HashMap<String, MutableList<String>> = HashMap<String, MutableList<String>>()
        alt.labelDefs.forEach { s, mutableList ->
            mutableList.forEach { e ->
                processSingleLabel(e, labelledTypes, res)
            }
        }
        alt.ruleRefs.forEach { s, mutableList ->
            val labels = labelledTypes.getOrDefault(s, LinkedList<String>())
            // Either all the elements have labels or none of them should have labels
            // If they have labels we ignore them here
            if (labels.size > 0 && mutableList.size != labels.size) {
                debugMsg("    Adding elements excluded by $s")
                res.add(Element(s, s, mutableList.size > 1, toExclude = labels))
            } else if (labels.size == 0) {
                debugMsg("    Adding elements not labelled $s")
                val multiple = mutableList.size > 1 || isMultiple(mutableList[0])
                res.add(Element(s, s, multiple))
            }
        }
        alt.tokenRefs.forEach { s, mutableList ->
            if (!s.startsWith("'")) {
                val nLabels = labelledTypes.getOrDefault(s, LinkedList<String>()).size
                // Either all the elements have labels or none of them should have labels
                // If they have labels we ignore them here
                if (nLabels > 0 && mutableList.size != nLabels) {
                    throw UnsupportedOperationException()
                } else if (nLabels == 0) {
                    val multiple = mutableList.size > 1 || isMultiple(mutableList[0])
                    res.add(Element(s, TOKEN_TYPE, multiple))
                }
            }
        }
        return res
    }

    private fun processSingleLabel(e: LabelElementPair, labelledTypes: java.util.HashMap<String, MutableList<String>>, res: MutableSet<Element>) {
        when (e.type) {
            LabelType.RULE_LIST_LABEL -> {
                val type = (e.element as RuleRefAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Element(name, type, true))
            }
            LabelType.RULE_LABEL -> {
                val type = (e.element as RuleRefAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Element(name, type, false))
            }
            LabelType.TOKEN_LABEL -> {
                val type = (e.element as GrammarAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Element(name, TOKEN_TYPE, false))
            }
            else -> throw UnsupportedOperationException("Label type ${e.type}")
        }
    }

    fun produceMetamodel(antlrGrammarCode: String) : Metamodel {
        val tool = Tool()
        val root = tool.parseGrammarFromString(antlrGrammarCode)
        val grammar = tool.createGrammar(root)
        tool.removeListeners()
        tool.addListener(object : ANTLRToolListener {
            override fun warning(msg: ANTLRMessage?) {
            }

            override fun info(msg: String?) {
            }

            override fun error(msg: ANTLRMessage?) {
            }
        })
        tool.process(grammar, false)

        val entities = LinkedList<Entity>()

        grammar.rules.forEach { s, rule ->
            try {
                debugMsg("Considering rule $s")
                if (rule is LeftRecursiveRule) {
                    debugMsg("  It is recursice ($s)")
                    val superclass = Entity(s, emptySet(), isAbstract = true)
                    entities.add(superclass)
                    if (rule.altLabels != null) {
                        rule.altLabels.forEach { altLabel ->
                            val name = altLabel.key
                            val altAlts = altLabel.value.map { el -> el.b }
                            processAltAsts(altAlts, entities, name, externalSuperclass = superclass)
                        }
                    }
                } else {
                    processRule(entities, rule, s)
                }
            } catch (e: RuntimeException) {
                throw RuntimeException("Error processing rule $s", e)
            }
        }
        return Metamodel(entities)
    }

    private fun processRule(entities: LinkedList<Entity>, rule: Rule, s: String) {
        processAlternatives(rule.alt.toList(), entities, s)
    }

    private fun processAlternatives(alternativesRaw: Collection<Alternative>, entities: LinkedList<Entity>, s: String) {
        val elements: MutableSet<Element> = HashSet<Element>()
        val alternatives = alternativesRaw.filter { alt -> alt != null }
        if (alternatives.size == 0) {
            throw IllegalArgumentException("No alternatives for $s")
        }
        if (alternatives[0].ast.altLabel != null) {
            // this is abstract
            val superclass = Entity(s, elements, isAbstract = true)
            entities.add(superclass)
            alternatives.forEach { alt ->
                entities.add(Entity(alt.ast.altLabel.text, considerAlternative(alt), superclass = superclass))
            }
        } else {
            alternatives.forEach { alt -> elements.addAll(considerAlternative(alt)) }
            entities.add(Entity(s, elements))
        }
    }

    private fun isToken(grammarAST: GrammarAST) : Boolean {
        return grammarAST.children.find { c -> !(c is TerminalAST) } == null
    }

    private fun processAltAsts(altAsts: Collection<AltAST>, entities: LinkedList<Entity>, s: String, externalSuperclass: Entity?) {
        val elements: MutableSet<Element> = HashSet()
        if (altAsts.size == 0) {
            throw IllegalArgumentException("No alternatives for $s")
        }
        altAsts.forEach { altAst ->
            altAst.children.forEach { el ->
                when (el) {
                    is TerminalAST -> elements.add(Element(el.token.text, TOKEN_TYPE, false))
                    is RuleRefAST -> elements.add(Element(el.text, el.text, false))
                    is GrammarAST -> if (el.childCount !=2) {
                        throw UnsupportedOperationException()
                    } else {
                        val name = (el.children[0] as GrammarAST).text
                        val subEl = el.children[1]
                        when (subEl) {
                            is RuleRefAST -> elements.add(Element(name, el.text, false))
                            is GrammarAST -> if (isToken(subEl)) {
                                elements.add(Element(name, TOKEN_TYPE, false))
                            } else {
                                throw UnsupportedOperationException()
                            }
                            else -> throw UnsupportedOperationException()
                        }
                    }
                    else -> throw UnsupportedOperationException()
                }
            }
        }
        // remove duplicates
        val elementNames = elements.map { e -> e.name }
        elementNames.forEach { name ->
            val elementsToMerge = elements.filter { e -> e.name == name }
            if (elementsToMerge.count() != 1) {
                // all must have the same type
                elementsToMerge.forEach { e -> if (elementsToMerge[0].type != e.type) {
                    throw UnsupportedOperationException()
                } }
                val multiple = elementsToMerge.find{ e -> e.multiple} != null
                elements.removeAll(elementsToMerge)
                elements.add(Element(name, elementsToMerge[0].type, multiple))
            }
        }
        val entity = Entity(s, elements, superclass=externalSuperclass)
        entities.add(entity)
    }

}