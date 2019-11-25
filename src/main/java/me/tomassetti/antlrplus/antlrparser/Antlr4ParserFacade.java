package me.tomassetti.antlrplus.antlrparser;

import org.antlr.v4.parse.ANTLRParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Antlr4ParserFacade {

    public ANTLRv4Parser.GrammarSpecContext parseString(String code) {
        InputStream inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        return parseStream(inputStream);
    }

    public ANTLRv4Parser.GrammarSpecContext parseFile(File file) throws FileNotFoundException {
        return parseStream(new FileInputStream(file));
    }

    public ANTLRv4Parser.GrammarSpecContext parseStream(InputStream inputStream) {
        try {
            ANTLRv4Lexer lexer = new ANTLRv4Lexer(new org.antlr.v4.runtime.ANTLRInputStream(inputStream));
            TokenStream tokens = new CommonTokenStream(lexer);
            ANTLRv4Parser parser = new ANTLRv4Parser(tokens);
            return parser.grammarSpec();
        } catch (IOException e) {
            throw new RuntimeException("That is unexpected", e);
        }
    }

}
