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

class ParseTreeFactoryTest {

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

    @Test fun pythonMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/python/Python3.g4"))
        val metamodel = ParseTreeToAstMapper().produceMetamodel(code)

        assertEquals(
                PtEntity("single_input", setOf(
                    simpleToken("NEWLINE"),
                    simpleChild("simple_stmt"),
                    simpleChild("compound_stmt"))),
                metamodel.byName("single_input"))

        assertEquals(
                PtEntity("file_input", setOf(
                        multipleToken("NEWLINE"),
                        simpleToken("EOF"),
                        multipleChild("stmt"))),
                metamodel.byName("file_input"))

        assertEquals(
                PtEntity("eval_input", setOf(
                        multipleToken("NEWLINE"),
                        simpleToken("EOF"),
                        simpleChild("testlist"))),
                metamodel.byName("eval_input"))

        assertEquals(
                PtEntity("decorator", setOf(
                        simpleToken("NEWLINE"),
                        simpleChild("dotted_name"),
                        simpleChild("arglist"))),
                metamodel.byName("decorator"))

        assertEquals(
                PtEntity("decorators", setOf(
                        multipleChild("decorator"))),
                metamodel.byName("decorators"))

        assertEquals(
                PtEntity("decorated", setOf(
                        simpleChild("classdef"),
                        simpleChild("funcdef"),
                        simpleChild("decorators"))),
                metamodel.byName("decorated"))

        assertEquals(
                PtEntity("funcdef", setOf(
                        simpleToken("DEF"),
                        simpleToken("NAME"),
                        simpleChild("parameters"),
                        simpleChild("test"),
                        simpleChild("suite"))),
                metamodel.byName("funcdef"))

        assertEquals(
                PtEntity("expr_stmt", setOf(
                        multipleChild("testlist_star_expr"),
                        simpleChild("augassign"),
                        multipleChild("yield_expr"),
                        simpleChild("testlist"))),
                metamodel.byName("expr_stmt"))

        assertEquals(
                PtEntity("testlist_star_expr", setOf(
                        multipleChild("test"),
                        multipleChild("star_expr"))),
                metamodel.byName("testlist_star_expr"))

    }

    @Test fun javaMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/java/Java8.g4"))
        val metamodel = ParseTreeToAstMapper().produceMetamodel(code)

        assertEquals(
                PtEntity("classType", setOf(
                        multipleChild("annotation"),
                        simpleToken("Identifier"),
                        simpleChild("typeArguments"),
                        simpleChild("classOrInterfaceType"))),
                metamodel.byName("classType"))

        assertEquals(
                PtEntity("classType_lf_classOrInterfaceType", setOf(
                        multipleChild("annotation"),
                        simpleToken("Identifier"),
                        simpleChild("typeArguments"))),
                metamodel.byName("classType_lf_classOrInterfaceType"))

        assertEquals(
                PtEntity("classType_lfno_classOrInterfaceType", setOf(
                        multipleChild("annotation"),
                        simpleToken("Identifier"),
                        simpleChild("typeArguments"))),
                metamodel.byName("classType_lfno_classOrInterfaceType"))
    }

    @Test fun sandyMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        val metamodel = mapper.produceMetamodel(code)

        assertEquals(
                PtEntity("sandyFile", setOf(
                        multipleChild("lines", "line"))),
                metamodel.byName("sandyFile"))

        assertEquals(
                PtEntity("line", setOf(
                        simpleChild("statement"),
                        simpleToken("NEWLINE"),
                        simpleToken("EOF"))),
                metamodel.byName("line"))

        assertEquals(
                PtEntity("statement", setOf(), isAbstract = true),
                metamodel.byName("statement"))

        assertEquals(
                PtEntity("varDeclarationStatement", setOf(
                        simpleChild("varDeclaration")), superclass = metamodel.byName("statement")),
                metamodel.byName("varDeclarationStatement"))

        assertEquals(
                PtEntity("assignmentStatement", setOf(
                        simpleChild("assignment")), superclass = metamodel.byName("statement")),
                metamodel.byName("assignmentStatement"))

        assertEquals(
                PtEntity("expression", setOf(), isAbstract = true),
                metamodel.byName("expression"))

        assertEquals(
                PtEntity("binaryOperation", setOf(
                        simpleChild("left", "expression"),
                        simpleChild("right", "expression"),
                        simpleToken("operator")), superclass = metamodel.byName("expression")),
                metamodel.byName("binaryOperation"))

        assertEquals(
                PtEntity("parenExpression", setOf(
                        simpleChild("expression"),
                        simpleToken("LPAREN"),
                        simpleToken("RPAREN")), superclass = metamodel.byName("expression")),
                metamodel.byName("parenExpression"))

        assertEquals(
                PtEntity("varReference", setOf(
                        simpleToken("ID")), superclass = metamodel.byName("expression")),
                metamodel.byName("varReference"))

        assertEquals(
                PtEntity("minusExpression", setOf(
                        simpleChild("expression"),
                        simpleToken("MINUS")), superclass = metamodel.byName("expression")),
                metamodel.byName("minusExpression"))

        assertEquals(
                PtEntity("intLiteral", setOf(
                        simpleToken("INTLIT")), superclass = metamodel.byName("expression")),
                metamodel.byName("intLiteral"))

        assertEquals(
                PtEntity("decimalLiteral", setOf(
                        simpleToken("DECLIT")), superclass = metamodel.byName("expression")),
                metamodel.byName("decimalLiteral"))
    }

    fun printTreeToString(code: String, extractors: ExtractorsMap) : String {
        val astRoot = sandyParserFacade.parseString("var a = 1 + 2")
        val root = extractors.toElement(astRoot) as PtElement
        val ss = ByteArrayOutputStream()
        val ps = PrintStream(ss)
        printTree("root", root, destination = ps)
        return ss.toString(Charsets.UTF_8.name())
    }

    @Test fun sandyExtractors() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        val pair = mapper.produceMetamodelAndExtractors(code, SandyParser::class.java)
        val metamodel = pair.first
        val extractors = pair.second

        assertEquals("""root: sandyFile
  lines: line
    statement: varDeclarationStatement
      varDeclaration: varDeclaration
        assignment: assignment
          ASSIGN: '='
          expression: binaryOperation
            right: intLiteral
              INTLIT: '2'
            operator: '+'
            left: intLiteral
              INTLIT: '1'
          ID: 'a'
        VAR: 'var'
    EOF: '<EOF>'
""", printTreeToString("var a = 1 + 2", extractors))
    }

    /*@Test fun alwaysIgnoreToken() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        mapper.alwaysIgnoreThisToken("EOF")
        mapper.alwaysIgnoreThisToken("VAR")
        mapper.alwaysIgnoreThisToken("ASSIGN")
        val pair = mapper.produceMetamodelAndExtractors(code, SandyParser::class.java)
        val metamodel = pair.first
        val extractors = pair.second

        assertEquals("""root: sandyFile
  lines: line
    statement: varDeclarationStatement
      varDeclaration: varDeclaration
        assignment: assignment
          expression: binaryOperation
            right: intLiteral
              INTLIT: '2'
            operator: '+'
            left: intLiteral
              INTLIT: '1'
          ID: 'a'
""", printTreeToString("var a = 1 + 2", extractors))
    }

    @Test fun markAsTransparentOneStep() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        mapper.alwaysIgnoreThisToken("EOF", -1)
        mapper.alwaysIgnoreThisToken("NEWLINE")
        mapper.alwaysIgnoreThisToken("VAR")
        mapper.alwaysIgnoreThisToken("ASSIGN")
        mapper.markAsTransparent("varDeclarationStatement")
        val pair = mapper.produceMetamodelAndExtractors(code, SandyParser::class.java)
        val metamodel = pair.first
        val extractors = pair.second

        assertEquals("""root: sandyFile
  lines: line
    statement: varDeclaration
      assignment: assignment
        expression: binaryOperation
          right: intLiteral
            INTLIT: '2'
          operator: '+'
          left: intLiteral
            INTLIT: '1'
        ID: 'a'
""", printTreeToString("var a = 1 + 2", extractors))
    }

    @Test fun markAsTransparentTwoConsecutiveStepsExtraction() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        mapper.alwaysIgnoreThisToken("EOF", -1)
        mapper.alwaysIgnoreThisToken("NEWLINE")
        mapper.alwaysIgnoreThisToken("VAR")
        mapper.alwaysIgnoreThisToken("ASSIGN")
        mapper.markAsTransparent("line")
        mapper.markAsTransparent("varDeclarationStatement")
        val pair = mapper.produceMetamodelAndExtractors(code, SandyParser::class.java)
        val metamodel = pair.first
        val extractors = pair.second

        assertEquals("""root: sandyFile
  lines: varDeclaration
    assignment: assignment
      expression: binaryOperation
        right: intLiteral
          INTLIT: '2'
        operator: '+'
        left: intLiteral
          INTLIT: '1'
      ID: 'a'
""", printTreeToString("var a = 1 + 2", extractors))
    }

    @Test fun markAsTransparentTwoConsecutiveStepsMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/SandyParser.g4"))
        val mapper = ParseTreeToAstMapper()
        mapper.alwaysIgnoreThisToken("EOF", -1)
        mapper.alwaysIgnoreThisToken("NEWLINE")
        mapper.alwaysIgnoreThisToken("VAR")
        mapper.alwaysIgnoreThisToken("ASSIGN")
        mapper.markAsTransparent("line")
        mapper.markAsTransparent("varDeclarationStatement")
        val pair = mapper.produceMetamodelAndExtractors(code, SandyParser::class.java)
        val metamodel = pair.first
        assertEquals(setOf("sandyFile", "statement", "assignmentStatement", "varDeclaration", "assignment",
                "expression", "decimalLiteral", "minusExpression", "intLiteral", "parenExpression",
                "binaryOperation", "varReference"), metamodel.entities.map { e -> e.name }.toSet())
        assertEquals(PtEntity("sandyFile", setOf(multipleChild("lines", "statement"))), metamodel.byName("sandyFile"))
        assertEquals(PtEntity("varDeclaration", setOf(simpleChild("assignment"))), metamodel.byName("varDeclaration"))
    }*/

    /*@Test fun pythonExtractors() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/python/Python3.g4"))
        val mapper = ParseTreeToAstMapper()
        val pair = mapper.produceMetamodelAndExtractors(code, Python3Parser::class.java)
        val metamodel = pair.first
        val extractors = pair.second

        val astRoot = pythonParserFacade.parseStream(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"))
        println(astRoot.javaClass)
        ///val = metamodel.byName("single_input").byName("simple_stmt")
        val res = extractors.toElement(astRoot, metamodel)
        println(res.PtEntity)
        printTree("root", res)
        println(res.get("NEWLINE"))
        println(res.get("compound_stmt"))
        println(res.get("simple_stmt"))
        //println(mapper.extractors.get("single_input", "simple_stmt").get(astRoot, el))
    }*/

}