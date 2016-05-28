package me.tomassetti.antlrplus;

import me.tomassetti.antlrplus.antlrparser.ANTLRv4Parser;
import me.tomassetti.antlrplus.antlrparser.Antlr4ParserFacade;
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
        Antlr4ParserFacade facade = new Antlr4ParserFacade();
        ANTLRv4Parser.GrammarSpecContext grammarSpec = facade.parseStream(inputStream);
    }

}
