package me.tomassetti.antlrplus.metamodel;

public class Relation {
    public enum Type {
        CONTAINMENT,
        REFERENCE
    };
    public enum Multiplicity {
        ONE,
        MANY
    }
    private String name;
    private Relation type;
    private Multiplicity multiplicity;
    private Entity source;
    private Entity target;

    public Relation(String name, Relation type, Multiplicity multiplicity, Entity source, Entity target) {
        this.name = name;
        this.type = type;
        this.multiplicity = multiplicity;
        this.source = source;
        this.target = target;
    }
}
