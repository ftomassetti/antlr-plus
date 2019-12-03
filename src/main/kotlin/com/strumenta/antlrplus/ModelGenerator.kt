package com.strumenta.antlrplus

import com.strumenta.antlrplus.antlrparser.Antlr4ParserFacade
import me.tomassetti.antlrplus.metamodel.mapping.AntlrReflectionMapper
import java.io.File
import org.snt.inmemantlr.GenericParser



fun main(args: Array<String>) {
//    val antlr4ParserFacade = Antlr4ParserFacade()
//    val grammarParseTree = antlr4ParserFacade.parseFile(File("/Users/federico/repos/VerilogParser/src/main/antlr/VerilogParser.g4"))
//    val grammarAst = grammarParseTree.toAst()
//    grammarAst.rules.forEach {
//        println("[${it.name}]")
//        if (it is ParserRule) {
//            it.block.alternatives.forEach {
//                println("alt ${it.name ?: "<unnamed>"}")
//            }
//        }
//    }
    //AntlrReflectionMapper()

//    val grammarLexerFile = File("/Users/federico/repos/VerilogParser/src/main/antlr/VerilogLexer.g4")
//    val grammarParserFile = File("/Users/federico/repos/VerilogParser/src/main/antlr/VerilogParser.g4")
//    val gp = GenericParser(grammarLexerFile, grammarParserFile)
//    gp()
//    val parserClass = gp.allCompiledObjects.forEach { println(it) }
}