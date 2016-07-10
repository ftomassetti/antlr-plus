package me.tomassetti.ast

import kotlin.collections.*

import org.antlr.v4.Tool
import org.antlr.v4.parse.ANTLRParser
import org.antlr.v4.runtime.ANTLRInputStream
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

        assertEquals(Entity("single_input", setOf(simpleToken("NEWLINE"), simpleChild("simple_stmt"), simpleChild("compound_stmt"))), metamodel.byName("single_input"))
    }

    @Test fun javaMetamodel() {
        val code = convert(this.javaClass.getResourceAsStream("/me/tomassetti/antlrplus/java/Java8.g4"))
        val metamodel = ParseTreeToAstMapper().produceMetamodel(code)
    }

}