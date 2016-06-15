package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.AntlrTreeNavigator;
import me.tomassetti.antlrplus.ParserFacade;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.*;

import static me.tomassetti.antlrplus.AntlrTreeNavigator.allChildrenIterable;

public class AntlrTreeNavigatorTest {

    private ParserFacade<Python3Parser.Single_inputContext, Python3Parser> parserFacade = new ParserFacade<Python3Parser.Single_inputContext, Python3Parser>() {
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


    @Test
    public void testAllChildrenIterable() {
        Python3Parser.Single_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"));
        List<ParseTree> elements = new LinkedList<>();
        for (ParseTree element : allChildrenIterable(astRoot)) {
            elements.add(element);
        }
        assertEquals(43, elements.size());

        assertEquals("Simple_stmtContext",elements.get(0).getClass().getSimpleName());
        assertEquals("Small_stmtContext",elements.get(1).getClass().getSimpleName());
        assertEquals("Expr_stmtContext",elements.get(2).getClass().getSimpleName());
        assertEquals("ExprContext",elements.get(10).getClass().getSimpleName());
        assertEquals("Xor_exprContext",elements.get(11).getClass().getSimpleName());
        assertEquals("AtomContext",elements.get(18).getClass().getSimpleName());
        assertEquals("TerminalNodeImpl",elements.get(19).getClass().getSimpleName());
        assertEquals("TrailerContext",elements.get(20).getClass().getSimpleName());
        assertEquals("TerminalNodeImpl",elements.get(42).getClass().getSimpleName());

        assertEquals("print(\"Hello world!\")",elements.get(0).getText().trim());
        assertEquals("print(\"Hello world!\")",elements.get(1).getText().trim());
        assertEquals("print(\"Hello world!\")",elements.get(2).getText().trim());
        assertEquals("print(\"Hello world!\")",elements.get(10).getText().trim());
        assertEquals("(\"Hello world!\")",elements.get(20).getText().trim());
        assertEquals("",elements.get(42).getText().trim());
    }

}
