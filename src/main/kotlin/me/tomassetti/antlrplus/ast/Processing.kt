package me.tomassetti.antlrplus.ast

import me.tomassetti.antlrplus.parsetree.PtEntity
import me.tomassetti.antlrplus.parsetree.PtFeature
import me.tomassetti.antlrplus.parsetree.PtMetamodel
import me.tomassetti.antlrplus.parsetree.TOKEN_TYPE
import java.util.*

/**
 * Map the PtMetamodel to the AST Metamodel and the PtElements to the AST Elements
 */
class Mapper {

    fun toAstMetamodel(ptMetamodel: PtMetamodel) : Metamodel {
        val metamodel = Metamodel()
        ptMetamodel.entities.forEach { toAstEntity(ptMetamodel, metamodel, it) }
        return metamodel
    }

    private fun toAstEntity(ptMetamodel: PtMetamodel, metamodel: Metamodel, ptEntity: PtEntity) : Entity {
        if (!metamodel.hasEntity(ptEntity.name)) {
            metamodel.addEntity(Entity(ptEntity.name,
                    ptEntity.features.map { toAstFeature(ptMetamodel, metamodel, it) }.toSet(),
                    abstract = ptEntity.isAbstract,
                    superEntities = if (ptEntity.superclass == null) emptySet() else setOf(toAstEntity(ptMetamodel, metamodel, ptEntity.superclass)) ))
        }
        return metamodel.byName(ptEntity.name)
    }

    private fun toAstFeature(ptMetamodel: PtMetamodel, metamodel: Metamodel, feature: PtFeature) : Feature {
        when (feature.type) {
            TOKEN_TYPE -> return Property(feature.name, Datatype.STRING, feature.multiple)
            else -> return Containment(feature.name, toAstEntity(ptMetamodel, metamodel, ptMetamodel.byName(feature.type)), feature.multiple)
        }
    }
}

/**
 * Given a Model (-> a root element) it produces a transformed Model
 */
interface ModelTransformer {
    fun transform(original:Element) : Element
}

/**
 * Transform the AST metamodel
 */
class MetamodelTransformation(val originalMetamodel: Metamodel) {
    private var specificTransformers = HashMap<String, ModelTransformer>()


    /**
     * It returns me something with the interface of an Element
     * which transforms values before returning them to me, so lazily.
     */
    fun transform(original: Element) = transformer.transform(original)

    private fun drop(entity: Entity, feature: Feature) {

    }
}

object IdentityModelTransformer : ModelTransformer {
    override fun transform(original: Element): Element {
        return original
    }

}
