package me.tomassetti.antlrplus.metamodel.mapping;


import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Multiplicity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class MetamodelPrinter {

    private boolean includePos = false;

    public void setIncludePos(boolean includePos) {
        this.includePos = includePos;
    }

    private boolean isPosProperty(Property property) {
        return property.getName().equals(AntlrReflectionMapper.START_LINE.getName())
                || property.getName().equals(AntlrReflectionMapper.START_COLUMN.getName())
                || property.getName().equals(AntlrReflectionMapper.END_LINE.getName())
                || property.getName().equals(AntlrReflectionMapper.END_COLUMN.getName());
    }

    public void print(AntlrReflectionMapper reflectionMapper, PrintStream out) {
        List<Entity> entities = new LinkedList<>();
        entities.addAll(reflectionMapper.allKnownEntities());
        entities.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
        for (Entity e : entities) {
            out.println((e.isAbstract()?"ABSTRACT ":"") + "ENTITY " + e.getName() + (e.getParent().isPresent()?" : "+e.getParent().get().getName():""));
            for (Property p : e.getProperties()) {
                if (includePos || !isPosProperty(p)) {
                    out.println("    property " + p.getName() + " -> " + p.getDatatype()+ (p.getMultiplicity()== Multiplicity.MANY?"*":""));
                }
            }
            for (Relation r : e.getRelations()) {
                out.println("    relation " + r.getName() +" -> "+ r.getTarget().getName()+ (r.getMultiplicity()== Multiplicity.MANY?"*":""));
            }
            out.println();
        }
    }
}
