package com.strumenta.antlrplus

import com.strumenta.antlrplus.antlrparser.Antlr4ParserFacade
import java.io.File

fun main(args: Array<String>) {
    val antlr4ParserFacade = Antlr4ParserFacade()
    val grammar = antlr4ParserFacade.parseFile(File("/Users/federico/repos/VerilogParser/src/main/antlr/VerilogParser.g4"))
}