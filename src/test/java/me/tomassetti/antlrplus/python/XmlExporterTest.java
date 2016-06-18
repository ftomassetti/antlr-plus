package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.ParserFacade;
import me.tomassetti.antlrplus.metamodel.mapping.ReflectionMapper;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.model.OrderedElement;
import me.tomassetti.antlrplus.xml.XmlExporter;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import static org.junit.Assert.*;

public class XmlExporterTest {

    @Test
    public void generic() throws NoSuchMethodException {
        Class clazz = Python3Parser.File_inputContext.class;
        {Method method = clazz.getMethod("stmt");
        Type listType = method.getGenericReturnType();
        if (listType instanceof ParameterizedType) {
            Type elementType = ((ParameterizedType) listType).getActualTypeArguments()[0];
            System.out.println("ELEMENT TYPE "+elementType);
        }}
        {Method method = clazz.getMethod("NEWLINE");
            Type listType = method.getGenericReturnType();
            if (listType instanceof ParameterizedType) {
                Type elementType = ((ParameterizedType) listType).getActualTypeArguments()[0];
                System.out.println("ELEMENT TYPE "+elementType);
            }}

    }

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
    public void parse() {
        Python3Parser.Single_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"));
        String name = Python3Parser.ruleNames[astRoot.getRuleIndex()];
        System.out.println(name);
        //String xmlCode = new XmlExporter().toXmlString(astRoot);

    }

    @Test
    public void toXml() {
        Python3Parser.Single_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"));
        OrderedElement element = new ReflectionMapper(Python3Parser.ruleNames).toElement(astRoot, Optional.empty());
        assertEquals("<root type=\"Single_input\">\n" +
                "    <simple_stmt type=\"Simple_stmt\">\n" +
                "        <small_stmt type=\"Small_stmt\">\n" +
                "            <expr_stmt type=\"Expr_stmt\">\n" +
                "                <testlist_star_expr type=\"Testlist_star_expr\">\n" +
                "                    <test type=\"Test\">\n" +
                "                        <or_test type=\"Or_test\">\n" +
                "                            <and_test type=\"And_test\">\n" +
                "                                <not_test type=\"Not_test\">\n" +
                "                                    <comparison type=\"Comparison\">\n" +
                "                                        <star_expr type=\"Star_expr\">\n" +
                "                                            <expr type=\"Expr\">\n" +
                "                                                <xor_expr type=\"Xor_expr\">\n" +
                "                                                    <and_expr type=\"And_expr\">\n" +
                "                                                        <shift_expr type=\"Shift_expr\">\n" +
                "                                                            <arith_expr type=\"Arith_expr\">\n" +
                "                                                                <term type=\"Term\">\n" +
                "                                                                    <factor type=\"Factor\">\n" +
                "                                                                        <power type=\"Power\">\n" +
                "                                                                            <atom type=\"Atom\">\n" +
                "                                                                                <NAME><![CDATA[print]]></NAME>\n" +
                "                                                                            </atom>\n" +
                "                                                                            <trailer type=\"Trailer\">\n" +
                "                                                                                <arglist type=\"Arglist\">\n" +
                "                                                                                    <argument type=\"Argument\">\n" +
                "                                                                                        <test type=\"Test\">\n" +
                "                                                                                            <or_test type=\"Or_test\">\n" +
                "                                                                                                <and_test type=\"And_test\">\n" +
                "                                                                                                    <not_test type=\"Not_test\">\n" +
                "                                                                                                        <comparison type=\"Comparison\">\n" +
                "                                                                                                            <star_expr type=\"Star_expr\">\n" +
                "                                                                                                                <expr type=\"Expr\">\n" +
                "                                                                                                                    <xor_expr type=\"Xor_expr\">\n" +
                "                                                                                                                        <and_expr type=\"And_expr\">\n" +
                "                                                                                                                            <shift_expr type=\"Shift_expr\">\n" +
                "                                                                                                                                <arith_expr type=\"Arith_expr\">\n" +
                "                                                                                                                                    <term type=\"Term\">\n" +
                "                                                                                                                                        <factor type=\"Factor\">\n" +
                "                                                                                                                                            <power type=\"Power\">\n" +
                "                                                                                                                                                <atom type=\"Atom\">\n" +
                "                                                                                                                                                    <string type=\"String\">\n" +
                "                                                                                                                                                        <STRING_LITERAL><![CDATA[\"Hello world!\"]]></STRING_LITERAL>\n" +
                "                                                                                                                                                    </string>\n" +
                "                                                                                                                                                </atom>\n" +
                "                                                                                                                                            </power>\n" +
                "                                                                                                                                        </factor>\n" +
                "                                                                                                                                    </term>\n" +
                "                                                                                                                                </arith_expr>\n" +
                "                                                                                                                            </shift_expr>\n" +
                "                                                                                                                        </and_expr>\n" +
                "                                                                                                                    </xor_expr>\n" +
                "                                                                                                                </expr>\n" +
                "                                                                                                            </star_expr>\n" +
                "                                                                                                        </comparison>\n" +
                "                                                                                                    </not_test>\n" +
                "                                                                                                </and_test>\n" +
                "                                                                                            </or_test>\n" +
                "                                                                                        </test>\n" +
                "                                                                                    </argument>\n" +
                "                                                                                </arglist>\n" +
                "                                                                            </trailer>\n" +
                "                                                                        </power>\n" +
                "                                                                    </factor>\n" +
                "                                                                </term>\n" +
                "                                                            </arith_expr>\n" +
                "                                                        </shift_expr>\n" +
                "                                                    </and_expr>\n" +
                "                                                </xor_expr>\n" +
                "                                            </expr>\n" +
                "                                        </star_expr>\n" +
                "                                    </comparison>\n" +
                "                                </not_test>\n" +
                "                            </and_test>\n" +
                "                        </or_test>\n" +
                "                    </test>\n" +
                "                </testlist_star_expr>\n" +
                "            </expr_stmt>\n" +
                "        </small_stmt>\n" +
                "        <NEWLINE><![CDATA[\n" +
                "]]></NEWLINE>\n" +
                "    </simple_stmt>\n" +
                "</root>\n", new XmlExporter().toXmlString(element, "root"));
    }

}
