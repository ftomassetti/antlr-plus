package me.tomassetti.antlrplus.metamodel;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class Entity {
    private String name;
    private List<Property> properties;
    private List<Relation> relations;
    private Optional<Entity> parent = Optional.empty();

    public Optional<Entity> getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return "Entity{" +
                "name='" + name + '\'' +
                ", properties=" + properties +
                ", relations=" + relations +
                ", parent=" + parent +
                '}';
    }

    public String getName() {
        return name;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void addProperty(Property property) {
        this.properties.add(property);
        this.properties.sort(new Comparator<Property>() {
            @Override
            public int compare(Property o1, Property o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public void addRelation(Relation relation) {
        this.relations.add(relation);
        this.relations.sort(new Comparator<Relation>() {
            @Override
            public int compare(Relation o1, Relation o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public Entity(String name) {

        this.name = name;
        this.properties = new LinkedList<>();
        this.relations = new LinkedList<>();
    }

    public Optional<Relation> getRelation(String name){
        return relations.stream().filter(r -> r.getName().equals(name)).findFirst();
    }
}
