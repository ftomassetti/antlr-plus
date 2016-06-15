package me.tomassetti.antlrplus.metamodel;

public abstract class Feature {

    private String name;

    public Feature(String name) {
        this.name = name;
    }

    public boolean isProperty() {
        return this instanceof Property;
    }

    public boolean isRelation() {
        return this instanceof Relation;
    }

    public Relation asRelation() {
        return (Relation)this;
    }

    public Property asProperty() {
        return (Property)this;
    }

    public String getName() {
        return name;
    }

}
