package me.tomassetti.ast

import me.tomassetti.antlrplus.ast.*
import org.antlr.runtime.tree.CommonTree
import org.antlr.v4.Tool
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.tool.*
import org.antlr.v4.tool.ast.*
import java.util.*


class ReflectionElement(val entity: Entity,
                        val extractors: Map<String, Extractor>,
                        val instance: ParserRuleContext,
                        val parent: Element? = null) : Element {

    override fun entity(): Entity = entity

    override fun parent(): Element? = parent

    override fun get(name: String) : Any? {
        return (extractors[name] ?: throw IllegalArgumentException(name)).get(instance, entity.byName(name), this)
    }

    override fun toString(): String{
        return "ReflectionElement(entity=$entity)"
    }
}

class ParseTreeToAstMapper() {

    var debug = false

    private var tokensToIgnore = HashSet<String>()

    fun alwaysIgnoreThisToken(token: String) {
        tokensToIgnore.add(token)
    }

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

    private fun considerAlternative(entityName: String, alt : Alternative, extractors: ExtractorsMap, parserClass: Class<out Parser>?) : Set<Feature>{
        debugMsg("  Considering alternative")
        val res = HashSet<Feature>()
        val labelledTypes : java.util.HashMap<String, MutableList<String>> = HashMap<String, MutableList<String>>()
        alt.labelDefs.forEach { s, mutableList ->
            mutableList.forEach { e ->
                if (!tokensToIgnore.contains((e.element as RuleRefAST).text)) {
                    processSingleLabel(entityName, e, labelledTypes, res, extractors, parserClass)
                }
            }
        }
        alt.ruleRefs.forEach { s, mutableList ->
            val labels = labelledTypes.getOrDefault(s, LinkedList<String>())
            // Either all the elements have labels or none of them should have labels
            // If they have labels we ignore them here
            if (labels.size > 0 && mutableList.size != labels.size) {
                debugMsg("    Adding elements excluded by $s")
                res.add(Feature(s, s, mutableList.size > 1))
                if (parserClass != null) {
                    extractors.addTokenExtractorWithExclusions(parserClass, entityName, s, labels)
                }
            } else if (labels.size == 0) {
                debugMsg("    Adding elements not labelled $s")
                val multiple = mutableList.size > 1 || isMultiple(mutableList[0])
                res.add(Feature(s, s, multiple))
                if (parserClass != null) {
                    extractors.addTokenExtractor(parserClass, entityName, s)
                }
            }
        }
        alt.tokenRefs.forEach { s, mutableList ->
            if (!s.startsWith("'") && !tokensToIgnore.contains(s)) {
                val nLabels = labelledTypes.getOrDefault(s, LinkedList<String>()).size
                // Either all the elements have labels or none of them should have labels
                // If they have labels we ignore them here
                if (nLabels > 0 && mutableList.size != nLabels) {
                    throw UnsupportedOperationException()
                } else if (nLabels == 0) {
                    val multiple = mutableList.size > 1 || isMultiple(mutableList[0])
                    res.add(Feature(s, TOKEN_TYPE, multiple))
                    if (parserClass != null) {
                        extractors.addTokenExtractor(parserClass, entityName, s)
                    }
                }
            }
        }
        return res
    }

    private fun processSingleLabel(entityName:String, e: LabelElementPair, labelledTypes: java.util.HashMap<String, MutableList<String>>,
                                   res: MutableSet<Feature>, extractors: ExtractorsMap, parserClass: Class<out Parser>?) {
        when (e.type) {
            LabelType.RULE_LIST_LABEL -> {
                val type = (e.element as RuleRefAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes.get(type)?.add(name)
                res.add(Feature(name, type, true))
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
                res.add(Feature(name, type, false))
                if (parserClass != null) {
                    extractors.addNodeExtractor(parserClass, entityName, name)
                }
            }
            LabelType.TOKEN_LABEL -> {
                val type = (e.element as GrammarAST).text
                if (!tokensToIgnore.contains(type)) {
                    val name = (e.label as GrammarAST).text
                    if (!(labelledTypes.containsKey(type))) {
                        labelledTypes.put(type, LinkedList<String>())
                    }
                    labelledTypes.get(type)?.add(name)
                    res.add(Feature(name, TOKEN_TYPE, false))
                    if (parserClass != null) {
                        extractors.addTokenExtractor(parserClass, entityName, name)
                    }
                }
            }
            else -> throw UnsupportedOperationException("Label type ${e.type}")
        }
    }

    fun produceMetamodel(antlrGrammarCode: String) : Metamodel {
        return internalProduceMetamodel(antlrGrammarCode, null).first
    }

    fun produceMetamodelAndExtractors(antlrGrammarCode: String, parserClass: Class<out Parser>) : Pair<Metamodel, ExtractorsMap> {
        return internalProduceMetamodel(antlrGrammarCode, parserClass)
    }

    /**
     * If the parserClass is passed it also describe the mapping
     */
    private fun internalProduceMetamodel(antlrGrammarCode: String, parserClass: Class<out Parser>? = null) : Pair<Metamodel, ExtractorsMap> {
        val grammar = grammarFrom(antlrGrammarCode)

        val metamodel = Metamodel()
        val extractors = ExtractorsMap(metamodel)

        grammar.rules.forEach { s, rule ->
            try {
                debugMsg("Considering rule $s")
                if (rule is LeftRecursiveRule) {
                    debugMsg("  It is recursive ($s)")
                    val superclass = Entity(s, emptySet(), isAbstract = true)
                    metamodel.addEntity(superclass)
                    if (rule.altLabels != null) {
                        rule.altLabels.forEach { altLabel ->
                            val name = altLabel.key
                            debugMsg("    Considering label $altLabel")
                            val altAlts = altLabel.value.map { el -> el.b }
                            processAltAsts(altAlts, metamodel, extractors, name, parserClass, externalSuperclass = superclass)
                        }
                    }
                } else {
                    processRule(metamodel, extractors, rule, s, parserClass)
                }
            } catch (e: RuntimeException) {
                throw RuntimeException("Error processing rule $s", e)
            }
        }
        return Pair(metamodel, extractors)
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

    private fun processRule(metamodel: Metamodel, extractors: ExtractorsMap, rule: Rule, s: String, parserClass: Class<out Parser>?) {
        processAlternatives(rule.alt.toList(), metamodel, extractors, s, parserClass)
    }

    private fun processAlternatives(alternativesRaw: Collection<Alternative>, metamodel: Metamodel, extractors: ExtractorsMap, s: String,
                                    parserClass: Class<out Parser>?) {
        val elements: MutableSet<Feature> = HashSet<Feature>()
        val alternatives = alternativesRaw.filter { alt -> alt != null }
        if (alternatives.size == 0) {
            throw IllegalArgumentException("No alternatives for $s")
        }
        if (alternatives[0].ast.altLabel != null) {
            // this is abstract
            val superclass = Entity(s, elements, isAbstract = true)
            metamodel.addEntity(superclass)
            alternatives.forEach { alt ->
                metamodel.addEntity(Entity(alt.ast.altLabel.text, considerAlternative(alt.ast.altLabel.text, alt, extractors, parserClass), superclass = superclass))
            }
        } else {
            alternatives.forEach { alt -> elements.addAll(considerAlternative(s, alt, extractors, parserClass)) }
            metamodel.addEntity(Entity(s, elements))
        }
    }

    private fun isToken(grammarAST: GrammarAST) : Boolean {
        return grammarAST.children.find { c -> !(c is TerminalAST) } == null
    }

    private fun processAltAsts(altAsts: Collection<AltAST>,
                               metamodel: Metamodel,
                               extractors: ExtractorsMap,
                               entityName: String, parserClass: Class<out Parser>?,
                               externalSuperclass: Entity?) {
        val elements: MutableSet<Feature> = HashSet()
        if (altAsts.size == 0) {
            throw IllegalArgumentException("No alternatives for $entityName")
        }
        altAsts.forEach { altAst ->
            altAst.children.forEach { el ->
                when (el) {
                    is TerminalAST -> {
                        if (!tokensToIgnore.contains(el.token.text)) {
                            debugMsg("      Adding terminal ${el.token.text}")
                            elements.add(Feature(el.token.text, TOKEN_TYPE, false))
                            if (parserClass != null) {
                                extractors.addTokenExtractor(parserClass, entityName, el.token.text)
                            }
                        }
                    }
                    is RuleRefAST -> {
                        debugMsg("      Adding rule ${el.text}")
                        elements.add(Feature(el.text, el.text, false))
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
                                debugMsg("      Adding rule with label (name: '$name', subEl: ${subEl.text})")
                                elements.add(Feature(name, subEl.text, false))
                                if (parserClass != null) {
                                    extractors.addNodeExtractor(parserClass, entityName, name)
                                }
                            }
                            is GrammarAST -> if (isToken(subEl)) {
                                elements.add(Feature(name, TOKEN_TYPE, false))
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
                elements.add(Feature(name, elementsToMerge[0].type, multiple))
            }
        }
        val entity = Entity(entityName, elements, superclass=externalSuperclass)
        metamodel.addEntity(entity)
    }

}