package me.tomassetti.antlrplus.parsetree

import java.io.PrintStream
import java.lang.System.out

interface PtElement {
    fun entity() : PtEntity
    fun parent() : PtElement?
    fun get(name: String) : Any?
}

fun printTree(role:String, el: PtElement, indentation:String="", destination: PrintStream = out) {
    destination.println("$indentation$role: ${el.entity().name}")
    el.entity().features.forEach { f ->
        val res = el.get(f.name)
        when (res) {
            null -> "nothing to do"
            is ReflectionPtElement -> printTree(f.name, res, indentation + "  ", destination)
            is String -> destination.println("$indentation  ${f.name}: '${res}'")
            is List<*> -> res.forEach { el ->
                when (el) {
                    is ReflectionPtElement -> printTree(f.name, el, indentation + "  ", destination)
                    is String -> destination.println("$indentation  ${f.name}: '$el'")
                    else -> throw IllegalArgumentException("${el?.javaClass}")
                }
            }
            else -> throw IllegalArgumentException("${res?.javaClass}")
        }
    }
}