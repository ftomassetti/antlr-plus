package me.tomassetti.antlrplus.antlrparser

import org.antlr.v4.runtime.CommonTokenStream
import java.io.*
import java.nio.charset.StandardCharsets

class Antlr4ParserFacade {

    fun parseString(code: String): ANTLRv4Parser.GrammarSpecContext {
        val inputStream = ByteArrayInputStream(code.toByteArray(StandardCharsets.UTF_8))
        return parseStream(inputStream)
    }

    @Throws(FileNotFoundException::class)
    fun parseFile(file: File): ANTLRv4Parser.GrammarSpecContext {
        return parseStream(FileInputStream(file))
    }

    fun parseStream(inputStream: InputStream): ANTLRv4Parser.GrammarSpecContext {
        try {
            val lexer = ANTLRv4Lexer(org.antlr.v4.runtime.ANTLRInputStream(inputStream))
            val tokens = CommonTokenStream(lexer)
            val parser = ANTLRv4Parser(tokens)
            return parser.grammarSpec()
        } catch (e: IOException) {
            throw RuntimeException("That is unexpected", e)
        }

    }

}
