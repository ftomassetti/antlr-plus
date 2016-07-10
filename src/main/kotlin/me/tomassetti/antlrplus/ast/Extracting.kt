package me.tomassetti.antlrplus.ast

import me.tomassetti.ast.ReflectionFeature
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.ParserRuleContext
import java.util.*

interface Extractor {
    fun get(instance:Any, element: Feature) : Any
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
        fun convert(raw: Any) : Any {
            when (raw) {
                is ParserRuleContext -> return extractorsMap.toElement(raw, metamodel)
                else -> throw UnsupportedOperationException("${raw.javaClass}")
            }
        }
    }

    fun toElement(raw: ParserRuleContext, metamodel: Metamodel) : ReflectionFeature {
        val entityName = raw.javaClass.simpleName.removeSuffix("Context").decapitalize()
        val entity = metamodel.byName(entityName)
        val extractors = map[entityName] ?: throw IllegalArgumentException()
        return ReflectionFeature(entity, extractors, raw)
    }

    fun addTokenExtractor(parserClass: Class<out Parser>, entityName: String, propertyName: String) {
        val ruleClass = parserClass.declaredClasses.find { c -> c.simpleName == "${entityName.capitalize()}Context" } ?: throw IllegalArgumentException("Cannot find ruleClass named '${entityName.capitalize()}Context'")
        val field = ruleClass.declaredFields.find { f -> f.name == propertyName }
        val method = ruleClass.declaredMethods.find { m -> m.name == propertyName }
        var extractor : Extractor? = null
        if (field != null) {
            extractor = object : BasicExtractor(metamodel, this) {
                override fun get(instance: Any, element: Feature): Any {
                    return convert(field.get(instance))
                }
            }
        } else if (field == null && method != null){
            extractor = object : BasicExtractor(metamodel, this) {
                override fun get(instance: Any, element: Feature): Any {
                    return convert(method.invoke(instance))
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