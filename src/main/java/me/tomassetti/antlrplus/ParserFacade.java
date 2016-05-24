package me.tomassetti.antlrplus;

import org.antlr.v4.runtime.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class ParserFacade<R extends ParserRuleContext, P extends Parser> {

    public ParserFacade() {
    }

    protected abstract Lexer getLexer(ANTLRInputStream antlrInputStream);

    protected abstract P getParser(TokenStream tokens);

    protected abstract R getRoot(P parser);

    public R parseString(String code) {
        InputStream inputStream = new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
        return parseStream(inputStream);
    }

    public R parseFile(File file) throws FileNotFoundException {
        return parseStream(new FileInputStream(file));
    }

    public R parseStream(InputStream inputStream) {
        try {
            Lexer lexer = getLexer(new org.antlr.v4.runtime.ANTLRInputStream(inputStream));
            TokenStream tokens = new CommonTokenStream(lexer);
            return getRoot(getParser(tokens));
        } catch (IOException e) {
            throw new RuntimeException("That is unexpected", e);
        }
    }
}
