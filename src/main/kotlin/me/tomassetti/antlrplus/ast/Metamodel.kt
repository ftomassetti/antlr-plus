package me.tomassetti.antlrplus.ast

import java.util.*

val TOKEN_TYPE = "<String>"

data class Feature(val name: String, val type: String, val multiple: Boolean) {
    override fun toString(): String {
        val desc = if (type == TOKEN_TYPE) "Token" else type
        val mult = if (multiple) "*" else ""
        return "$name:$desc$mult"
    }
}

class Entity(val name: String, val elements: Set<Feature>, val superclass: Entity? = null, val isAbstract: Boolean = false) {
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

    fun byName(name: String) : Feature {
        return elements.find { e -> e.name == name } ?: throw IllegalArgumentException(name)
    }
}

class Metamodel() {

    val entities : MutableList<Entity> = LinkedList<Entity>()

    fun addEntity(entity: Entity) {
        entities.add(entity)
    }

    fun byName(name: String) : Entity {
        val e = entities.find { e -> e.name == name }
        if (e == null) {
            throw IllegalArgumentException("Unknown entity $name")
        } else {
            return e
        }
    }
}

fun simpleToken(name: String) = Feature(name, TOKEN_TYPE, false)

fun multipleToken(name: String) = Feature(name, TOKEN_TYPE, true)

fun simpleChild(name: String, type: String = name) = Feature(name, type, false)

fun multipleChild(name: String, type: String = name) = Feature(name, type, true)