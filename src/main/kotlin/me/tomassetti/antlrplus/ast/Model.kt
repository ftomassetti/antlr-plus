package me.tomassetti.antlrplus.ast

interface Element {
    fun entity() : Entity
    fun parent() : Element?
    fun get(feature: Feature) : Any?
}