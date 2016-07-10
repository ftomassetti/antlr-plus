package me.tomassetti.antlrplus.ast

import me.tomassetti.ast.ReflectionElement
import java.io.PrintStream
import java.lang.System.out

interface Element {
    fun entity() : Entity
    fun parent() : Element?
    fun get(name: String) : Any?
}

fun printTree(role:String, el: Element, indentation:String="", destination: PrintStream = out) {
    destination.println("$indentation$role: ${el.entity().name}")
    el.entity().features.forEach { f ->
        val res = el.get(f.name)
        when (res) {
            null -> "nothing to do"
            is ReflectionElement -> printTree(f.name, res, indentation + "  ", destination)
            is String -> destination.println("$indentation  ${f.name}: '${res}'")
            is List<*> -> res.forEach { el ->
                when (el) {
                    is ReflectionElement -> printTree(f.name, el, indentation + "  ", destination)
                    is String -> destination.println("$indentation  ${f.name}: '$el'")
                    else -> throw IllegalArgumentException("${el?.javaClass}")
                }
            }
            else -> throw IllegalArgumentException("${res?.javaClass}")
        }
    }
}