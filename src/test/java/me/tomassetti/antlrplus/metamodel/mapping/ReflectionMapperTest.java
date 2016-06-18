package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.ParserFacade;
import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Multiplicity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.metamodel.mapping.ReflectionMapper;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.python.Python3Lexer;
import me.tomassetti.antlrplus.python.Python3Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Test
    public void ifStmt() {
        Entity entity = new ReflectionMapper(Python3Parser.ruleNames).getEntity(Python3Parser.If_stmtContext.class);
        assertEquals("If_stmt", entity.getName());
        assertEquals(false, entity.getParent().isPresent());
        assertEquals(2, entity.getProperties().size());
        assertEquals(true, entity.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList()).contains("ELSE"));
        assertEquals(true, entity.getProperties().stream().map(p -> p.getName()).collect(Collectors.toList()).contains("IF"));
        assertEquals(3, entity.getRelations().size());
        assertEquals(true, entity.getRelations().stream().map(r -> r.getName()).collect(Collectors.toList()).contains("condition"));
        assertEquals(true, entity.getRelations().stream().map(r -> r.getName()).collect(Collectors.toList()).contains("body"));
        assertEquals(true, entity.getRelations().stream().map(r -> r.getName()).collect(Collectors.toList()).contains("elifs"));
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

    @Test
    public void testIsListOf() throws NoSuchFieldException {
        assertEquals(true, ReflectionMapper.isListOf(Python3Parser.If_stmtContext.class.getField("elifs").getGenericType(), Python3Parser.ElifClauseContext.class));
    }

    @Test
    public void testFieldsOfType() {
        assertEquals(2, ReflectionMapper.fieldsOfType(Python3Parser.If_stmtContext.class, Python3Parser.ElifClauseContext.class).size());
    }
}
