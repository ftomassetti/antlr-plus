package me.tomassetti.ast

import org.antlr.runtime.tree.CommonTree
import org.antlr.v4.Tool
import org.antlr.v4.runtime.Parser
import org.antlr.v4.tool.*
import org.antlr.v4.tool.ast.*
import java.util.*

val TOKEN_TYPE = "<String>"

data class Element(val name: String, val type: String, val multiple: Boolean) {
    override fun toString(): String {
        val desc = if (type == TOKEN_TYPE) "Token" else type
        val mult = if (multiple) "*" else ""
        return "$name:$desc$mult"
    }
}

interface Extractor<I, R> {
    fun get(instance:I, element: Element) : R
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

    class ExtractorsMap {
        private val map = HashMap<String, MutableMap<String, Extractor<*, *>>>()

        fun get(entityName:String, elementName: String) : Extractor<*, *> {
           val res = map[entityName]?.get(elementName)
           res ?: throw IllegalArgumentException()
           return res
        }

        fun set(entityName:String, elementName: String, extractor: Extractor<*, *>) {
            map.putIfAbsent(entityName, HashMap<String, Extractor<*, *>>())
            map[entityName]?.put(elementName, extractor)
        }

        fun  addTokenExtractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
            val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
            val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
            val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
            var extractor : Extractor<*, *>? = null
            if (field == null) {
                extractor = object : Extractor<Object, String> {
                    override fun get(instance: Object, element: Element): String {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
            } else {
                extractor = object : Extractor<Object, String> {
                    override fun get(instance: Object, element: Element): String {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
            }
            if (extractor == null) {
                throw IllegalArgumentException()
            } else {
                set(entityName, propertyName, extractor)
            }
        }

        fun  addNodeExtractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
            val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
            val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
            val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
            var extractor : Extractor<*, *>? = null
            if (field == null) {
                extractor = object : Extractor<Object, String> {
                    override fun get(instance: Object, element: Element): String {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
            } else {
                extractor = object : Extractor<Object, String> {
                    override fun get(instance: Object, element: Element): String {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
            }
            if (extractor == null) {
                throw IllegalArgumentException()
            } else {
                set(entityName, propertyName, extractor)
            }
        }

        fun  addTokenExtractorWithExclusions(parserClass: Class<out Parser>, entityName: String, propertyName: String, labels: MutableList<String>?) {
            // TODO fixme!
            val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
            val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
            val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
            var extractor : Extractor<*, *>? = null
            if (field == null) {
                extractor = object : Extractor<Object, String> {
                    override fun get(instance: Object, element: Element): String {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
            } else {
                extractor = object : Extractor<Object, String> {
                    override fun get(instance: Object, element: Element): String {
                        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                }
            }
            if (extractor == null) {
                throw IllegalArgumentException()
            } else {
                set(entityName, propertyName, extractor)
            }
        }
    }

    var debug = false
    val extractors = ExtractorsMap()

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

    private fun considerAlternative(entityName: String, alt : Alternative, parserClass: Class<out Parser>?) : Set<Element>{
        debugMsg("  Considering alternative")
        val res = HashSet<Element>()
        val labelledTypes : java.util.HashMap<String, MutableList<String>> = HashMap<String, MutableList<String>>()
        alt.labelDefs.forEach { s, mutableList ->
            mutableList.forEach { e ->
                processSingleLabel(entityName, e, labelledTypes, res, parserClass)
            }
        }
        alt.ruleRefs.forEach { s, mutableList ->
            val labels = labelledTypes.getOrDefault(s, LinkedList<String>())
            // Either all the elements have labels or none of them should have labels
            // If they have labels we ignore them here
            if (labels.size > 0 && mutableList.size != labels.size) {
                debugMsg("    Adding elements excluded by $s")
                res.add(Element(s, s, mutableList.size > 1))
                if (parserClass != null) {
                    extractors.addTokenExtractorWithExclusions(parserClass, entityName, s, labels)
                }
            } else if (labels.size == 0) {
                debugMsg("    Adding elements not labelled $s")
                val multiple = mutableList.size > 1 || isMultiple(mutableList[0])
                res.add(Element(s, s, multiple))
                if (parserClass != null) {
                    extractors.addTokenExtractor(parserClass, entityName, s)
                }
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
                    if (parserClass != null) {
                        extractors.addTokenExtractor(parserClass, entityName, s)
                    }
                }
            }
        }
        return res
    }

    private fun processSingleLabel(entityName:String, e: LabelElementPair, labelledTypes: java.util.HashMap<String, MutableList<String>>,
                                   res: MutableSet<Element>, parserClass: Class<out Parser>?) {
        when (e.type) {
            LabelType.RULE_LIST_LABEL -> {
                val type = (e.element as RuleRefAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Element(name, type, true))
                if (parserClass != null) {
                    extractors.addNodeExtractor(parserClass, entityName, name)
                }
            }
            LabelType.RULE_LABEL -> {
                val type = (e.element as RuleRefAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Element(name, type, false))
                if (parserClass != null) {
                    extractors.addNodeExtractor(parserClass, entityName, name)
                }
            }
            LabelType.TOKEN_LABEL -> {
                val type = (e.element as GrammarAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Element(name, TOKEN_TYPE, false))
                if (parserClass != null) {
                    extractors.addTokenExtractor(parserClass, entityName, name)
                }
            }
            else -> throw UnsupportedOperationException("Label type ${e.type}")
        }
    }

    /**
     * If the parserClass is passed it also describe the mapping
     */
    fun produceMetamodel(antlrGrammarCode: String, parserClass: Class<out Parser>? = null) : Metamodel {
        val grammar = grammarFrom(antlrGrammarCode)

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
                            processAltAsts(altAlts, entities, name, parserClass, externalSuperclass = superclass)
                        }
                    }
                } else {
                    processRule(entities, rule, s, parserClass)
                }
            } catch (e: RuntimeException) {
                throw RuntimeException("Error processing rule $s", e)
            }
        }
        return Metamodel(entities)
    }

    private fun grammarFrom(antlrGrammarCode: String): Grammar {
        val tool = Tool()
        val root = tool.parseGrammarFromString(antlrGrammarCode)
        val grammar = tool.createGrammar(root)

        // Errors are not relevant in this context
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
        return grammar
    }

    private fun processRule(entities: LinkedList<Entity>, rule: Rule, s: String, parserClass: Class<out Parser>?) {
        processAlternatives(rule.alt.toList(), entities, s, parserClass)
    }

    private fun processAlternatives(alternativesRaw: Collection<Alternative>, entities: LinkedList<Entity>, s: String,
                                    parserClass: Class<out Parser>?) {
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
                entities.add(Entity(alt.ast.altLabel.text, considerAlternative(s, alt, parserClass), superclass = superclass))
            }
        } else {
            alternatives.forEach { alt -> elements.addAll(considerAlternative(s, alt, parserClass)) }
            entities.add(Entity(s, elements))
        }
    }

    private fun isToken(grammarAST: GrammarAST) : Boolean {
        return grammarAST.children.find { c -> !(c is TerminalAST) } == null
    }

    private fun processAltAsts(altAsts: Collection<AltAST>, entities: LinkedList<Entity>,
                               entityName: String, parserClass: Class<out Parser>?,
                               externalSuperclass: Entity?) {
        val elements: MutableSet<Element> = HashSet()
        if (altAsts.size == 0) {
            throw IllegalArgumentException("No alternatives for $entityName")
        }
        altAsts.forEach { altAst ->
            altAst.children.forEach { el ->
                when (el) {
                    is TerminalAST -> {
                        elements.add(Element(el.token.text, TOKEN_TYPE, false))
                        if (parserClass != null) {
                            extractors.addTokenExtractor(parserClass, entityName, el.token.text)
                        }
                    }
                    is RuleRefAST -> {
                        elements.add(Element(el.text, el.text, false))
                        if (parserClass != null) {
                            extractors.addTokenExtractor(parserClass, entityName, el.text)
                        }
                    }
                    is GrammarAST -> if (el.childCount !=2) {
                        throw UnsupportedOperationException()
                    } else {
                        val name = (el.children[0] as GrammarAST).text
                        val subEl = el.children[1]
                        when (subEl) {
                            is RuleRefAST -> {
                                elements.add(Element(name, el.text, false))
                                if (parserClass != null) {
                                    extractors.addNodeExtractor(parserClass, entityName, name)
                                }
                            }
                            is GrammarAST -> if (isToken(subEl)) {
                                elements.add(Element(name, TOKEN_TYPE, false))
                                if (parserClass != null) {
                                    extractors.addTokenExtractor(parserClass, entityName, name)
                                }
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
        val entity = Entity(entityName, elements, superclass=externalSuperclass)
        entities.add(entity)
    }

}