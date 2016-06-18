package me.tomassetti.antlrplus.model;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;

import java.util.List;
import java.util.Optional;

public interface Element {
    Entity type();
    Optional<Element> getSingleRelation(Relation relation);
    Optional<Element> getSingleRelation(String name);
    Optional<Object> getSingleProperty(String name);
    void setSingleRelation(Relation relation, Element element);
    int getMultipleRelationCount(Relation relation);
    List<Element> getMultipleRelation(Relation relation);
    List<Object> getMultipleProperty(Property property);
    Element getMultipleRelationAt(Relation relation, int index);
    void addMultipleRelation(Relation relation, Element element);
    void removeMultipleRelationAt(Relation relation, int index);
    void setSingleProperty(Property property, Object value);
    Optional<Object> getSingleProperty(Property property);
    List<Element> getAllChildren();
    Optional<Element> getParent();
    Optional<Element> firstChild();
}
