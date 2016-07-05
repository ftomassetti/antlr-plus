package me.tomassetti.antlrplus.model;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.model.OrderedElement;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractOrderedElement implements OrderedElement {
    protected Entity entity;
    protected Optional<OrderedElement> parent;

    public AbstractOrderedElement(Entity entity, Optional<OrderedElement> parent) {
        this.entity = entity;
        this.parent = parent;
    }

    @Override
    public Optional<Object> getSingleProperty(String name) {
        return this.getSingleProperty(this.type().getProperty(name).get());
    }

    @Override
    public Optional<Element> getSingleRelation(String name) {
        Optional<Relation> relation = this.type().getRelation(name);
        if (!relation.isPresent()) {
            throw new IllegalArgumentException("Unknown relation " + name + " for entity "+entity.getName());
        }
        return this.getSingleRelation(relation.get());
    }

    @Override
    public Optional<Element> getParent() {
        if (parent.isPresent()) {
            return Optional.of(parent.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void setSingleRelation(Relation relation, Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMultipleRelationCount(Relation relation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Entity type() {
        return entity;
    }

    @Override
    public Optional<Element> firstChild() {
        if (getAllChildren().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(getAllChildren().get(0));
        }
    }

    @Override
    public Element getMultipleRelationAt(Relation relation, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMultipleRelation(Relation relation, Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMultipleRelationAt(Relation relation, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSingleProperty(Property property, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Element> getAllChildren() {
        List<Element> children = new LinkedList<>();
        for (Relation relation : this.type().getRelations()) {
            if (relation.isSingle()) {
                Optional<Element> child = this.getSingleRelation(relation);
                if (child.isPresent()) {
                    children.add(child.get());
                }
            } else {
                children.addAll(this.getMultipleRelation(relation));
            }
        }
        return children;
    }
}
