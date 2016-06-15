package me.tomassetti.antlrplus.metamodel;

public abstract class Feature {

    private String name;

    public Feature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
