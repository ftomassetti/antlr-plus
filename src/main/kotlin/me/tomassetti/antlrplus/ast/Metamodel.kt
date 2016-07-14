package me.tomassetti.antlrplus.ast

import me.tomassetti.antlrplus.parsetree.PtEntity

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

data class Containment(val name: String, val type: PtEntity, val multiple: Boolean) : me.tomassetti.antlrplus.ast.Feature {
    override fun name() = name
    override fun isMultiple() = multiple
}

data class Reference(val name: String, val type: PtEntity, val multiple: Boolean) : me.tomassetti.antlrplus.ast.Feature {
    override fun name() = name
    override fun isMultiple() = multiple
}