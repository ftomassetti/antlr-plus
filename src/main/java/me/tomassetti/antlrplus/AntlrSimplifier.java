package me.tomassetti.antlrplus;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.parse.*;


import java.io.*;

import org.antlr.v4.Tool;

/**
 * Created by federico on 28/05/16.
 */
public class AntlrSimplifier {

    public void simplify(File file) throws FileNotFoundException {
        simplify(new FileInputStream(file));
    }

    private void analyzeGrammar(ToolANTLRParser.grammarSpec_return grammar) {
        System.out.println("GRAMMAR TREE " +grammar.getTree());
    }

    public void simplify(InputStream inputStream) {
        Tool tool = new Tool();
        try {
            ToolANTLRLexer lexer = new ToolANTLRLexer(new org.antlr.runtime.ANTLRInputStream(inputStream), tool);
            org.antlr.runtime.TokenStream tokens = new org.antlr.runtime.CommonTokenStream(lexer);
            ToolANTLRParser parser = new ToolANTLRParser(tokens, tool);
            parser.setTreeAdaptor(new GrammarASTAdaptor());
            ANTLRParser.grammarSpec_return grammar = parser.grammarSpec();
            analyzeGrammar(grammar);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RecognitionException e) {
            throw new RuntimeException(e);
        }
    }

}
