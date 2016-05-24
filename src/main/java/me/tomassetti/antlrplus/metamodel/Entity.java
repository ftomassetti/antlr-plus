package me.tomassetti.antlrplus.metamodel;

import java.util.LinkedList;
import java.util.List;

public class Entity {
    private String name;
    private List<Property> properties;
    private List<Relation> relations;

    public Entity(String name) {
        this.name = name;
        this.properties = new LinkedList<>();
        this.relations = new LinkedList<>();
    }
}
