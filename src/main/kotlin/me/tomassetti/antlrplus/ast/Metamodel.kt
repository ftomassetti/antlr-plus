package me.tomassetti.antlrplus.ast

import java.util.*

enum class Datatype {
    STRING,
    INTEGER,
    BOOLEAN
}

interface Feature {
    fun name() : String
    fun isMultiple() : Boolean
    fun isSingle() = !isMultiple()
}

data class Property(val name: String, val type: Datatype, val multiple: Boolean) : me.tomassetti.antlrplus.ast.Feature {
    override fun name() = name
    override fun isMultiple() = multiple
}

data class Containment(val name: String, val type: Entity, val multiple: Boolean) : me.tomassetti.antlrplus.ast.Feature {
    override fun name() = name
    override fun isMultiple() = multiple
}

fun containMany(type: Entity, name: String) = Containment(name, type, true)
fun containOne(type: Entity, name: String = type.name) = Containment(name, type, false)

data class Reference(val name: String, val type: Entity, val multiple: Boolean) : me.tomassetti.antlrplus.ast.Feature {
    override fun name() = name
    override fun isMultiple() = multiple
}

data class Entity(val name:String, val features:Set<Feature>,
                  var abstract: Boolean = false,
                  val superEntities: Set<Entity> = emptySet()) {

}

data class Metamodel(val entities: MutableSet<Entity> = HashSet<Entity>()) {
    fun  hasEntity(name: String) : Boolean = entities.any { it.name == name }

    fun  addEntity(entity: Entity) : Unit {
        entities.add(entity)
    }

    fun  byName(name: String): Entity = entities.find { it.name == name } ?: throw IllegalArgumentException("Unknown entity $name")

}