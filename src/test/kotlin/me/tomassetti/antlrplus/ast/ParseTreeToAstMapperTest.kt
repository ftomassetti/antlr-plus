package me.tomassetti.ast

import me.tomassetti.antlrplus.ParserFacade
import me.tomassetti.antlrplus.ast.*
import me.tomassetti.antlrplus.python.Python3Lexer
import me.tomassetti.antlrplus.python.Python3Parser
import kotlin.collections.*

import org.antlr.v4.Tool
import org.antlr.v4.parse.ANTLRParser
import org.antlr.v4.runtime.ANTLRInputStream
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.TokenStream
import org.antlr.v4.tool.ANTLRMessage
import org.antlr.v4.tool.ANTLRToolListener
import org.antlr.v4.tool.Alternative
import org.antlr.v4.tool.LabelType
import org.antlr.v4.tool.ast.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.util.*
import kotlin.test.assertEquals

class ParseTreeToAstMapperTest {

    private val parserFacade = object : ParserFacade<Python3Parser.Single_inputContext, Python3Parser>() {
        override fun getLexer(antlrInputStream: ANTLRInputStream): Lexer {
            return Python3Lexer(antlrInputStream)
        }

        override fun getParser(tokens: TokenStream): Python3Parser {
            return Python3Parser(tokens)
        }

        override fun getRoot(parser: Python3Parser): Python3Parser.Single_inputContext {
            return parser.single_input()
        }
    }

    fun convert(inputStream: InputStream) : String {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        do {
            length = inputStream.read(buffer)
            if (length != -1) {
                result.write(buffer, 0, length)
            }
        } while (length != -1)
        return result.toString("UTF-8")
    }

    @Test fun pythonMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/python/Python3.g4"))
        val metamodel = ParseTreeToAstMapper().produceMetamodel(code)

        assertEquals(
                Entity("single_input", setOf(
                    simpleToken("NEWLINE"),
                    simpleChild("simple_stmt"),
                    simpleChild("compound_stmt"))),
                metamodel.byName("single_input"))

        assertEquals(
                Entity("file_input", setOf(
                        multipleToken("NEWLINE"),
                        simpleToken("EOF"),
                        multipleChild("stmt"))),
                metamodel.byName("file_input"))

        assertEquals(
                Entity("eval_input", setOf(
                        multipleToken("NEWLINE"),
                        simpleToken("EOF"),
                        simpleChild("testlist"))),
                metamodel.byName("eval_input"))

        assertEquals(
                Entity("decorator", setOf(
                        simpleToken("NEWLINE"),
                        simpleChild("dotted_name"),
                        simpleChild("arglist"))),
                metamodel.byName("decorator"))

        assertEquals(
                Entity("decorators", setOf(
                        multipleChild("decorator"))),
                metamodel.byName("decorators"))

        assertEquals(
                Entity("decorated", setOf(
                        simpleChild("classdef"),
                        simpleChild("funcdef"),
                        simpleChild("decorators"))),
                metamodel.byName("decorated"))

        assertEquals(
                Entity("funcdef", setOf(
                        simpleToken("DEF"),
                        simpleToken("NAME"),
                        simpleChild("parameters"),
                        simpleChild("test"),
                        simpleChild("suite"))),
                metamodel.byName("funcdef"))

        assertEquals(
                Entity("expr_stmt", setOf(
                        multipleChild("testlist_star_expr"),
                        simpleChild("augassign"),
                        multipleChild("yield_expr"),
                        simpleChild("testlist"))),
                metamodel.byName("expr_stmt"))

        assertEquals(
                Entity("testlist_star_expr", setOf(
                        multipleChild("test"),
                        multipleChild("star_expr"))),
                metamodel.byName("testlist_star_expr"))

    }

    @Test fun javaMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/java/Java8.g4"))
        val metamodel = ParseTreeToAstMapper().produceMetamodel(code)

        assertEquals(
                Entity("classType", setOf(
                        multipleChild("annotation"),
                        simpleToken("Identifier"),
                        simpleChild("typeArguments"),
                        simpleChild("classOrInterfaceType"))),
                metamodel.byName("classType"))

        assertEquals(
                Entity("classType_lf_classOrInterfaceType", setOf(
                        multipleChild("annotation"),
                        simpleToken("Identifier"),
                        simpleChild("typeArguments"))),
                metamodel.byName("classType_lf_classOrInterfaceType"))

        assertEquals(
                Entity("classType_lfno_classOrInterfaceType", setOf(
                        multipleChild("annotation"),
                        simpleToken("Identifier"),
                        simpleChild("typeArguments"))),
                metamodel.byName("classType_lfno_classOrInterfaceType"))
    }

    @Test fun pythonExtractors() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/python/Python3.g4"))
        val mapper = ParseTreeToAstMapper()
        val pair = mapper.produceMetamodelAndExtractors(code, Python3Parser::class.java)
        val metamodel = pair.first
        val extractors = pair.second

        val astRoot = parserFacade.parseStream(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"))
        println(astRoot.javaClass)
        ///val = metamodel.byName("single_input").byName("simple_stmt")
        val res = extractors.toElement(astRoot, metamodel)
        println(res)
        //println(mapper.extractors.get("single_input", "simple_stmt").get(astRoot, el))
    }

}