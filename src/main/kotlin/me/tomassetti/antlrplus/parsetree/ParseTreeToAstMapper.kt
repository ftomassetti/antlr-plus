package me.tomassetti.antlrplus.parsetree

import me.tomassetti.antlrplus.parsetree.Element
import org.antlr.runtime.tree.CommonTree
import org.antlr.v4.Tool
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.tool.*
import org.antlr.v4.tool.ast.*
import java.util.*

class ReflectionElement(val entity: PtEntity,
                        val extractors: Map<String, Extractor>,
                        val instance: ParserRuleContext,
                        val parent: Element? = null) : Element {

    override fun entity(): PtEntity = entity

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
    private var tokensTypesToIgnore = HashSet<Int>()
    private var transparentEntities = HashSet<String>()

    fun alwaysIgnoreThisToken(token: String, type: Int? = null) {
        tokensToIgnore.add(token)
        if (type != null) {
            tokensTypesToIgnore.add(type)
        }
    }

    fun markAsTransparent(type: String) {
        transparentEntities.add(type)
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

    private fun considerAlternative(entityName: String, alt : Alternative, extractors: ExtractorsMap, parserClass: Class<out Parser>?) : Set<PtFeature>{
        debugMsg("  Considering alternative")
        val res = HashSet<PtFeature>()
        val labelledTypes : HashMap<String, MutableList<String>> = HashMap()
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
                res.add(PtFeature(s, s, mutableList.size > 1))
                if (parserClass != null) {
                    extractors.addTokenExtractorWithExclusions(parserClass, entityName, s, labels)
                }
            } else if (labels.size == 0) {
                debugMsg("    Adding elements not labelled $s")
                val multiple = mutableList.size > 1 || isMultiple(mutableList[0])
                res.add(PtFeature(s, s, multiple))
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
                    res.add(PtFeature(s, TOKEN_TYPE, multiple))
                    if (parserClass != null) {
                        extractors.addTokenExtractor(parserClass, entityName, s)
                    }
                }
            }
        }
        return res
    }

    private fun processSingleLabel(entityName:String, e: LabelElementPair, labelledTypes: HashMap<String, MutableList<String>>,
                                   res: MutableSet<PtFeature>, extractors: ExtractorsMap, parserClass: Class<out Parser>?) {
        when (e.type) {
            LabelType.RULE_LIST_LABEL -> {
                val type = (e.element as RuleRefAST).text
                val name = (e.label as GrammarAST).text
                if (!(labelledTypes.containsKey(type))) {
                    labelledTypes.put(type, LinkedList<String>())
                }
                labelledTypes[type]?.add(name)
                res.add(PtFeature(name, type, true))
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
                labelledTypes[type]?.add(name)
                res.add(PtFeature(name, type, false))
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
                    labelledTypes[type]?.add(name)
                    res.add(PtFeature(name, TOKEN_TYPE, false))
                    if (parserClass != null) {
                        extractors.addTokenExtractor(parserClass, entityName, name)
                    }
                }
            }
            else -> throw UnsupportedOperationException("Label type ${e.type}")
        }
    }

    fun produceMetamodel(antlrGrammarCode: String) : PtMetamodel {
        return internalProduceMetamodel(antlrGrammarCode, null).first
    }

    fun produceMetamodelAndExtractors(antlrGrammarCode: String, parserClass: Class<out Parser>) : Pair<PtMetamodel, ExtractorsMap> {
        return internalProduceMetamodel(antlrGrammarCode, parserClass)
    }

    /**
     * If the parserClass is passed it also describe the mapping
     */
    private fun internalProduceMetamodel(antlrGrammarCode: String, parserClass: Class<out Parser>? = null) : Pair<PtMetamodel, ExtractorsMap> {
        val grammar = grammarFrom(antlrGrammarCode)

        val metamodel = PtMetamodel()
        val extractors = ExtractorsMap(metamodel)
        transparentEntities.forEach { te -> extractors.markAsTransparentType(te) }
        tokensTypesToIgnore.forEach { tt -> extractors.markAsTransparentTokenType(tt) }
        val transparentEntitiesMapping = HashMap<String, String>()

        grammar.rules.forEach { s, rule ->
            try {
                debugMsg("Considering rule $s")
                if (rule is LeftRecursiveRule) {
                    if (transparentEntities.contains(s)) {
                        throw IllegalStateException("The rule is transparent: $s")
                    }
                    debugMsg("  It is recursive ($s)")
                    val superclass = PtEntity(s, emptySet(), isAbstract = true)
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
                    val e = processRule(metamodel, extractors, rule, s, parserClass)
                    if (transparentEntities.contains(s)) {
                        if (e.isAbstract) {
                            throw IllegalArgumentException()
                        }
                        if (e.features.size != 1){
                            throw IllegalArgumentException("Type $s is marked as transparent, so it should have one feature but instead it has ${e.features.size}: ${e.features}")
                        }
                        val f = e.features.iterator().next()
                        if (f.multiple || f.type == TOKEN_TYPE) {
                            throw IllegalArgumentException()
                        }
                        transparentEntitiesMapping[s] = f.type
                    }
                }
            } catch (e: RuntimeException) {
                throw RuntimeException("Error processing rule $s", e)
            }
        }

        metamodel.entities.removeAll { e -> transparentEntities.contains(e.name) }
        metamodel.entities.forEach { e ->
            e.features.forEach { f ->
                if (transparentEntitiesMapping.containsKey(f.type)) {
                    f.type = transparentEntitiesMapping[f.type]!!
                }
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

    private fun processRule(metamodel: PtMetamodel, extractors: ExtractorsMap, rule: Rule, s: String, parserClass: Class<out Parser>?) : PtEntity {
        return processAlternatives(rule.alt.toList(), metamodel, extractors, s, parserClass)
    }

    private fun processAlternatives(alternativesRaw: Collection<Alternative>, metamodel: PtMetamodel, extractors: ExtractorsMap, s: String,
                                    parserClass: Class<out Parser>?)  : PtEntity {
        val elements: MutableSet<PtFeature> = HashSet()
        val alternatives = alternativesRaw.filter { alt -> alt != null }
        if (alternatives.size == 0) {
            throw IllegalArgumentException("No alternatives for $s")
        }
        if (alternatives[0].ast.altLabel != null) {
            // this is abstract
            val superclass = PtEntity(s, elements, isAbstract = true)
            metamodel.addEntity(superclass)
            alternatives.forEach { alt ->
                metamodel.addEntity(PtEntity(alt.ast.altLabel.text, considerAlternative(alt.ast.altLabel.text, alt, extractors, parserClass), superclass = superclass))
            }
            return superclass
        } else {
            alternatives.forEach { alt -> elements.addAll(considerAlternative(s, alt, extractors, parserClass)) }
            val e = PtEntity(s, elements)
            metamodel.addEntity(e)
            return e
        }
    }

    private fun isToken(grammarAST: GrammarAST) : Boolean {
        return grammarAST.children.find { c -> !(c is TerminalAST) } == null
    }

    private fun processAltAsts(altAsts: Collection<AltAST>,
                               metamodel: PtMetamodel,
                               extractors: ExtractorsMap,
                               entityName: String, parserClass: Class<out Parser>?,
                               externalSuperclass: PtEntity?) {
        val elements: MutableSet<PtFeature> = HashSet()
        if (altAsts.size == 0) {
            throw IllegalArgumentException("No alternatives for $entityName")
        }
        altAsts.forEach { altAst ->
            altAst.children.forEach { el ->
                when (el) {
                    is TerminalAST -> {
                        if (!tokensToIgnore.contains(el.token.text)) {
                            debugMsg("      Adding terminal ${el.token.text}")
                            elements.add(PtFeature(el.token.text, TOKEN_TYPE, false))
                            if (parserClass != null) {
                                extractors.addTokenExtractor(parserClass, entityName, el.token.text)
                            }
                        }
                    }
                    is RuleRefAST -> {
                        debugMsg("      Adding rule ${el.text}")
                        elements.add(PtFeature(el.text, el.text, false))
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
                                elements.add(PtFeature(name, subEl.text, false))
                                if (parserClass != null) {
                                    extractors.addNodeExtractor(parserClass, entityName, name)
                                }
                            }
                            is GrammarAST -> if (isToken(subEl)) {
                                elements.add(PtFeature(name, TOKEN_TYPE, false))
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
                elements.add(PtFeature(name, elementsToMerge[0].type, multiple))
            }
        }
        val entity = PtEntity(entityName, elements, superclass=externalSuperclass)
        metamodel.addEntity(entity)
    }

}