package me.tomassetti.antlrplus.parsetree

import me.tomassetti.antlrplus.ast.Element
import me.tomassetti.antlrplus.ast.Feature
import me.tomassetti.antlrplus.parsetree.ReflectionElement
import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import java.util.*

interface Extractor {
    fun get(instance:Any, feature: Feature, element: Element) : Any?
}

class ExtractorsMap(val metamodel: Metamodel) {
    private val map = HashMap<String, MutableMap<String, Extractor>>()
    private val transparentEntities = HashSet<String>()
    private val transparentTokens = HashSet<Int>()

    fun markAsTransparentType(type: String) {
        transparentEntities.add(type)
    }

    fun markAsTransparentTokenType(type: Int) {
        transparentTokens.add(type)
    }

    fun get(entityName:String, elementName: String) : Extractor {
        val res = map[entityName]?.get(elementName)
        res ?: throw IllegalArgumentException("Cannot find extractor for $entityName.$elementName. Extractor for the entity: ${map[entityName]?.keys}")
        return res
    }

    fun set(entityName:String, elementName: String, extractor: Extractor) {
        map.putIfAbsent(entityName, HashMap<String, Extractor>())
        map[entityName]?.put(elementName, extractor)
    }

    abstract class BasicExtractor(val extractorsMap: ExtractorsMap) : Extractor {
        fun convert(raw: Any?, parent: Element) : Any? {
            if (raw == null) {
                return null
            }
            when (raw) {
                is ParserRuleContext -> return extractorsMap.toElement(raw, parent)
                is TerminalNodeImpl -> return raw.text
                is CommonToken -> return raw.text
                is List<*> -> return raw.map { e -> convert(e, parent) }.filter { e -> e != null }
                else -> throw UnsupportedOperationException("${raw.javaClass}")
            }
        }
    }

    fun toEntityName(ruleClass: Class<out ParserRuleContext>) = ruleClass.simpleName.removeSuffix("Context").decapitalize()

    fun toElement(raw: ParserRuleContext, parent: Element? = null) : ReflectionElement? {
        val entityName = toEntityName(raw.javaClass)
        if (this.transparentEntities.contains(entityName)) {
            val children = raw.children.filter { f -> !isToBeIgnored(f) }
            when (children.size) {
                0 -> return null
                1 -> return toElement(children[0] as ParserRuleContext, parent)
                else -> throw IllegalStateException()
            }
        }
        val entity = metamodel.byName(entityName)
        val extractors = map[entityName] ?: throw IllegalArgumentException()
        return ReflectionElement(entity, extractors, raw, parent)
    }

    private fun isToBeIgnored(pt: ParseTree): Boolean {
        when (pt) {
            is ParserRuleContext -> return false//return !transparentEntities.contains(toEntityName(pt.javaClass))
            is TerminalNodeImpl -> return transparentTokens.contains(pt.symbol.type)
            else -> throw IllegalArgumentException(pt.javaClass.canonicalName)
        }
    }

    fun addTokenExtractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
        return extractor(parserClass, entityName, propertyName)
    }

    private fun extractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
        val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
        val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
        val method = ruleClass.declaredMethods.find { m -> m.name == propertyName && m.parameters.size == 0 }
        var extractor : Extractor? = null
        if (field != null) {
            extractor = object : BasicExtractor(this) {
                override fun get(instance: Any, feature: Feature, element: Element): Any? {
                    return convert(field.get(instance), element)
                }
            }
        } else if (field == null && method != null){
            extractor = object : BasicExtractor(this) {
                override fun get(instance: Any, feature: Feature, element: Element): Any? {
                    return convert(method.invoke(instance), element)
                }
            }
        }
        if (extractor == null) {
            throw IllegalArgumentException("No extractor found for property $propertyName in entity $entityName")
        } else {
            set(entityName, propertyName, extractor)
        }
    }

    fun  addNodeExtractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
        return extractor(parserClass, entityName, propertyName)
    }

    fun addTokenExtractorWithExclusions(parserClass: Class<out Parser>, entityName: String, propertyName: String, labels: MutableList<String>?) {
        // TODO fixme!
        val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
        val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
        val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
        var extractor : Extractor? = null
        if (field == null) {
            extractor = object : Extractor {
                override fun get(instance: Any, feature: Feature, element: Element): Any {
                    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        } else {
            extractor = object : Extractor {
                override fun get(instance: Any, feature: Feature, element: Element): Any {
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