package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.ParserFacade;
import me.tomassetti.antlrplus.XmlExporter;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;
import org.w3c.dom.Node;

public class XmlExporterTest {

    @Test
    public void parse() {
        ParserFacade<Python3Parser.Single_inputContext, Python3Parser> parserFacade = new ParserFacade<Python3Parser.Single_inputContext, Python3Parser>() {
            @Override
            protected Lexer getLexer(ANTLRInputStream antlrInputStream) {
                return new Python3Lexer(antlrInputStream);
            }

            @Override
            protected Python3Parser getParser(TokenStream tokens) {
                return new Python3Parser(tokens);
            }

            @Override
            protected Python3Parser.Single_inputContext getRoot(Python3Parser parser) {
                return parser.single_input();
            }
        };
        Python3Parser.Single_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"));
        String xmlCode = new XmlExporter().toXmlString(astRoot);
    }

}
