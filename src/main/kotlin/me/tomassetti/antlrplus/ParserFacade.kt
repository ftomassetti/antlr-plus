package me.tomassetti.antlrplus

import org.antlr.v4.runtime.*

import java.io.*
import java.nio.charset.StandardCharsets

abstract class ParserFacade<R : ParserRuleContext, P : Parser> {

    protected abstract fun getLexer(antlrInputStream: ANTLRInputStream): Lexer

    protected abstract fun getParser(tokens: TokenStream): P

    protected abstract fun getRoot(parser: P): R

    fun parseString(code: String): R {
        val inputStream = ByteArrayInputStream(code.toByteArray(StandardCharsets.UTF_8))
        return parseStream(inputStream)
    }

    @Throws(FileNotFoundException::class)
    fun parseFile(file: File): R {
        return parseStream(FileInputStream(file))
    }

    fun parseStream(inputStream: InputStream): R {
        try {
            val lexer = getLexer(ANTLRInputStream(inputStream))
            val tokens = CommonTokenStream(lexer)
            return getRoot(getParser(tokens))
        } catch (e: IOException) {
            throw RuntimeException("That is unexpected", e)
        }

    }
}
