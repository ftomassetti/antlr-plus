package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.ParserFacade;
import me.tomassetti.antlrplus.model.OrderedElement;
import me.tomassetti.antlrplus.python.Python3Lexer;
import me.tomassetti.antlrplus.python.Python3Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;

import java.util.Optional;

public class ReflectionElementTest {

    private ParserFacade<Python3Parser.File_inputContext, Python3Parser> parserFacade = new ParserFacade<Python3Parser.File_inputContext, Python3Parser>() {
        @Override
        protected Lexer getLexer(ANTLRInputStream antlrInputStream) {
            return new Python3Lexer(antlrInputStream);
        }

        @Override
        protected Python3Parser getParser(TokenStream tokens) {
            return new Python3Parser(tokens);
        }

        @Override
        protected Python3Parser.File_inputContext getRoot(Python3Parser parser) {
            return parser.file_input();
        }
    };

    @Test
    public void lineAndColumnsSimplest() {
        AntlrReflectionMapper reflectionMapper = new AntlrReflectionMapper(Python3Parser.ruleNames, Python3Lexer.class);
        reflectionMapper.setAddPositions(true);

        Python3Parser.File_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"));
        OrderedElement rootElement = new AntlrReflectionMapper(Python3Parser.ruleNames, Python3Lexer.class).toElement(astRoot, Optional.empty());

        assertEquals(1, rootElement.getSingleProperty("startLine").get());
        assertEquals(2, rootElement.getSingleProperty("endLine").get());
        assertEquals(0, rootElement.getSingleProperty("startColumn").get());
        assertEquals(0, rootElement.getSingleProperty("endColumn").get());
    }

    @Test
    public void lineAndColumnsSimple() {
        AntlrReflectionMapper reflectionMapper = new AntlrReflectionMapper(Python3Parser.ruleNames, Python3Lexer.class);
        reflectionMapper.setAddPositions(true);

        Python3Parser.File_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/main.py"));
        OrderedElement rootElement = new AntlrReflectionMapper(Python3Parser.ruleNames, Python3Lexer.class).toElement(astRoot, Optional.empty());

        assertEquals(1, rootElement.getSingleProperty("startLine").get());
        assertEquals(3, rootElement.getSingleProperty("endLine").get());
        assertEquals(0, rootElement.getSingleProperty("startColumn").get());
        assertEquals(0, rootElement.getSingleProperty("endColumn").get());
    }

    @Test
    public void lineAndColumnsCommon() {
        AntlrReflectionMapper reflectionMapper = new AntlrReflectionMapper(Python3Parser.ruleNames, Python3Lexer.class);
        reflectionMapper.setAddPositions(true);

        Python3Parser.File_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/common.py"));

        OrderedElement rootElement = new AntlrReflectionMapper(Python3Parser.ruleNames, Python3Lexer.class).toElement(astRoot, Optional.empty());

        assertEquals(1, rootElement.getSingleProperty("startLine").get());
        assertEquals(120, rootElement.getSingleProperty("endLine").get());
        assertEquals(0, rootElement.getSingleProperty("startColumn").get());
        assertEquals(23, rootElement.getSingleProperty("endColumn").get());
    }
}
