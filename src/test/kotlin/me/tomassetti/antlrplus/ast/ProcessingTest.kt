package me.tomassetti.antlrplus.ast

import me.tomassetti.antlrplus.ParserFacade
import me.tomassetti.antlrplus.parsetree.*
import me.tomassetti.antlrplus.Python3Lexer
import me.tomassetti.antlrplus.Python3Parser
import me.tomassetti.antlrplus.SandyLexer
import me.tomassetti.antlrplus.SandyParser
import me.tomassetti.antlrplus.parsetree.PtElement
import me.tomassetti.antlrplus.parsetree.ExtractorsMap
import me.tomassetti.antlrplus.parsetree.ParseTreeToAstMapper
import me.tomassetti.antlrplus.parsetree.printTree
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
import java.io.*
import java.util.*
import kotlin.test.assertEquals

class ProcessingTest {

    private val pythonParserFacade = object : ParserFacade<Python3Parser.Single_inputContext, Python3Parser>() {
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

    private val sandyParserFacade = object : ParserFacade<SandyParser.SandyFileContext, SandyParser>() {
        override fun getLexer(antlrInputStream: ANTLRInputStream): Lexer {
            return SandyLexer(antlrInputStream)
        }

        override fun getParser(tokens: TokenStream): SandyParser {
            return SandyParser(tokens)
        }

        override fun getRoot(parser: SandyParser): SandyParser.SandyFileContext {
            return parser.sandyFile()
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

    @Test fun sandyMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        val ptMetamodel = mapper.produceMetamodel(code)
        val metamodel = Mapper().toAstMetamodel(ptMetamodel)

        assertEquals(Entity("sandyFile", setOf(
                containMany(metamodel.byName("line"), "lines"))),
                metamodel.byName("sandyFile"))

        assertEquals(Entity("statement", setOf(), abstract = true),
                metamodel.byName("statement"))

        assertEquals(Entity("varDeclarationStatement", setOf(containOne(metamodel.byName("varDeclaration"))),
                superEntities = setOf(metamodel.byName("statement"))),
                metamodel.byName("varDeclarationStatement"))

        assertEquals(Entity("expression", setOf(), abstract = true),
                metamodel.byName("expression"))

        /*

        assertEquals(
                PtEntity("varDeclarationStatement", setOf(
                        simpleChild("varDeclaration")), superclass = ptMetamodel.byName("statement")),
                ptMetamodel.byName("varDeclarationStatement"))

        assertEquals(
                PtEntity("assignmentStatement", setOf(
                        simpleChild("assignment")), superclass = ptMetamodel.byName("statement")),
                ptMetamodel.byName("assignmentStatement"))

        assertEquals(
                PtEntity("expression", setOf(), isAbstract = true),
                ptMetamodel.byName("expression"))

        assertEquals(
                PtEntity("binaryOperation", setOf(
                        simpleChild("left", "expression"),
                        simpleChild("right", "expression"),
                        simpleToken("operator")), superclass = ptMetamodel.byName("expression")),
                ptMetamodel.byName("binaryOperation"))

        assertEquals(
                PtEntity("parenExpression", setOf(
                        simpleChild("expression"),
                        simpleToken("LPAREN"),
                        simpleToken("RPAREN")), superclass = ptMetamodel.byName("expression")),
                ptMetamodel.byName("parenExpression"))

        assertEquals(
                PtEntity("varReference", setOf(
                        simpleToken("ID")), superclass = ptMetamodel.byName("expression")),
                ptMetamodel.byName("varReference"))

        assertEquals(
                PtEntity("minusExpression", setOf(
                        simpleChild("expression"),
                        simpleToken("MINUS")), superclass = ptMetamodel.byName("expression")),
                ptMetamodel.byName("minusExpression"))

        assertEquals(
                PtEntity("intLiteral", setOf(
                        simpleToken("INTLIT")), superclass = ptMetamodel.byName("expression")),
                ptMetamodel.byName("intLiteral"))

        assertEquals(
                PtEntity("decimalLiteral", setOf(
                        simpleToken("DECLIT")), superclass = ptMetamodel.byName("expression")),
                ptMetamodel.byName("decimalLiteral"))*/
    }

}
