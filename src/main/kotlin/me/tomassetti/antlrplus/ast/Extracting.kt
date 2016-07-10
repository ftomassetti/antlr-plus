package me.tomassetti.antlrplus.ast

import me.tomassetti.ast.ReflectionElement
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import java.util.*

interface Extractor {
    fun get(instance:Any, element: Feature) : Any?
}

class ExtractorsMap(val metamodel: Metamodel) {
    private val map = HashMap<String, MutableMap<String, Extractor>>()

    fun get(entityName:String, elementName: String) : Extractor {
        val res = map[entityName]?.get(elementName)
        res ?: throw IllegalArgumentException("Cannot find extractor for $entityName.$elementName. Extractor for the entity: ${map[entityName]?.keys}")
        return res
    }

    fun set(entityName:String, elementName: String, extractor: Extractor) {
        map.putIfAbsent(entityName, HashMap<String, Extractor>())
        map[entityName]?.put(elementName, extractor)
    }

    abstract class BasicExtractor(val metamodel: Metamodel, val extractorsMap: ExtractorsMap) : Extractor {
        fun convert(raw: Any?) : Any? {
            if (raw == null) {
                return null
            }
            when (raw) {
                is ParserRuleContext -> return extractorsMap.toElement(raw, metamodel)
                is TerminalNodeImpl -> return raw.text
                is List<*> -> return raw.map { e -> convert(e) }
                else -> throw UnsupportedOperationException("${raw.javaClass}")
            }
        }
    }

    fun toElement(raw: ParserRuleContext, metamodel: Metamodel) : ReflectionElement {
        val entityName = raw.javaClass.simpleName.removeSuffix("Context").decapitalize()
        val entity = metamodel.byName(entityName)
        val extractors = map[entityName] ?: throw IllegalArgumentException()
        return ReflectionElement(entity, extractors, raw)
    }

    fun addTokenExtractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
        val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
        val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
        val method = ruleClass.declaredMethods.find { m -> m.name == propertyName && m.parameters.size == 0 }
        var extractor : Extractor? = null
        if (field != null) {
            extractor = object : BasicExtractor(metamodel, this) {
                override fun get(instance: Any, element: Feature): Any? {
                    return convert(field.get(instance))
                }
            }
        } else if (field == null && method != null){
            extractor = object : BasicExtractor(metamodel, this) {
                override fun get(instance: Any, element: Feature): Any? {
                    return convert(method.invoke(instance))
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
        val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
        val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
        val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
        var extractor : Extractor? = null
        if (field == null) {
            extractor = object : Extractor {
                override fun get(instance: Any, element: Feature): Any {
                    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        } else {
            extractor = object : Extractor {
                override fun get(instance: Any, element: Feature): Any {
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

    fun addTokenExtractorWithExclusions(parserClass: Class<out Parser>, entityName: String, propertyName: String, labels: MutableList<String>?) {
        // TODO fixme!
        val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
        val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
        val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
        var extractor : Extractor? = null
        if (field == null) {
            extractor = object : Extractor {
                override fun get(instance: Any, element: Feature): Any {
                    throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }
        } else {
            extractor = object : Extractor {
                override fun get(instance: Any, element: Feature): Any {
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