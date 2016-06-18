package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.ParserFacade;
import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Multiplicity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.metamodel.mapping.ReflectionMapper;
import me.tomassetti.antlrplus.model.Element;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ReflectionMapperTest {

    @Test
    public void singleInput() {
        Entity entity = new ReflectionMapper(Python3Parser.ruleNames).getEntity(Python3Parser.Single_inputContext.class);
        assertEquals("Single_input", entity.getName());
        assertEquals(false, entity.getParent().isPresent());
        assertEquals(1, entity.getProperties().size());
        assertEquals(new Property("NEWLINE", Property.Datatype.STRING, Multiplicity.ONE), entity.getProperties().get(0));
        assertEquals(2, entity.getRelations().size());

        Relation simple_stmt = entity.getRelation("simple_stmt").get();
        assertEquals(Relation.Type.CONTAINMENT, simple_stmt.getType());
        assertEquals(Multiplicity.ONE, simple_stmt.getMultiplicity());
        assertEquals("Single_input", simple_stmt.getSource().getName());
        assertEquals("Simple_stmt", simple_stmt.getTarget().getName());

        Relation compound_stmt = entity.getRelation("compound_stmt").get();
        assertEquals(Relation.Type.CONTAINMENT, compound_stmt.getType());
        assertEquals(Multiplicity.ONE, compound_stmt.getMultiplicity());
        assertEquals("Single_input", compound_stmt.getSource().getName());
        assertEquals("Compound_stmt", compound_stmt.getTarget().getName());
    }

    @Test
    public void passStmt() {
        Entity entity = new ReflectionMapper(Python3Parser.ruleNames).getEntity(Python3Parser.Pass_stmtContext.class);
        assertEquals("Pass_stmt", entity.getName());
        assertEquals(false, entity.getParent().isPresent());
        assertEquals(1, entity.getProperties().size());
        assertEquals(new Property("PASS", Property.Datatype.STRING, Multiplicity.ONE), entity.getProperties().get(0));
        assertEquals(0, entity.getRelations().size());
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
    public void toElement() {
        Python3Parser.Single_inputContext astRoot = parserFacade.parseStream(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/hello_world.py"));
        Element element = new ReflectionMapper(Python3Parser.ruleNames).toElement(astRoot, Optional.empty());
        Entity entity = element.type();
        assertEquals(true, element.getSingleRelation(entity.getRelation("simple_stmt").get()).isPresent());
        assertEquals(false, element.getSingleRelation(entity.getRelation("compound_stmt").get()).isPresent());
    }
}
