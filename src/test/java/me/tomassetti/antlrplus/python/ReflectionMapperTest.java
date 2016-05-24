package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Multiplicity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.metamodel.mapping.ReflectionMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReflectionMapperTest {

    @Test
    public void singleInput() {
        Entity entity = new ReflectionMapper(Python3Parser.ruleNames).getEntity(Python3Parser.Single_inputContext.class);
        assertEquals("Single_input", entity.getName());
        assertEquals(false, entity.getParent().isPresent());
        assertEquals(1, entity.getProperties().size());
        assertEquals(new Property("NEWLINE", Property.Datatype.STRING, Multiplicity.ONE), entity.getProperties().get(0));
        System.out.println(entity);
        assertEquals(2, entity.getRelations().size());

        assertEquals("simple_stmt", entity.getRelations().get(0).getName());
        assertEquals(Relation.Type.CONTAINMENT, entity.getRelations().get(0).getType());
        assertEquals(Multiplicity.ONE, entity.getRelations().get(0).getMultiplicity());
        assertEquals("Single_input", entity.getRelations().get(0).getSource().getName());
        assertEquals("Simple_stmt", entity.getRelations().get(0).getTarget().getName());

        assertEquals("compound_stmt", entity.getRelations().get(1).getName());
        assertEquals(Relation.Type.CONTAINMENT, entity.getRelations().get(1).getType());
        assertEquals(Multiplicity.ONE, entity.getRelations().get(1).getMultiplicity());
        assertEquals("Single_input", entity.getRelations().get(1).getSource().getName());
        assertEquals("Compound_stmt", entity.getRelations().get(1).getTarget().getName());
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
}
