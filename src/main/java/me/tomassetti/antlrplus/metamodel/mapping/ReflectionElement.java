package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.model.Element;
import org.antlr.v4.runtime.ParserRuleContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ReflectionElement implements Element {

    private ParserRuleContext wrapped;
    private Entity entity;
    private ReflectionMapper reflectionMapper;

    public ReflectionElement(ReflectionMapper reflectionMapper, ParserRuleContext wrapped, Entity entity) {
        this.reflectionMapper = reflectionMapper;
        this.wrapped = wrapped;
        this.entity = entity;
    }

    @Override
    public Entity type() {
        return entity;
    }

    @Override
    public List<Object> getMultipleProperty(Property property) {
        if (property.isSingle()) {
            throw new IllegalArgumentException();
        }
        List<Object> elements = new ArrayList<>();
        try {
            List<? extends Object> result = (List<? extends Object>)wrapped.getClass().getMethod(property.getName()).invoke(wrapped);
            elements.addAll(result);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    @Override
    public Optional<Element> getSingleRelation(Relation relation) {
        if (!relation.isSingle()) {
            throw new IllegalArgumentException();
        }
        try {
            ParserRuleContext result = (ParserRuleContext)wrapped.getClass().getMethod(relation.getName()).invoke(wrapped);
            if (result == null) {
                return Optional.empty();
            } else {
                return Optional.of(reflectionMapper.toElement(result));
            }
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassCastException e){
            throw new RuntimeException("Relation "+relation, e);
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
    public List<Element> getMultipleRelation(Relation relation) {
        if (relation.isSingle()) {
            throw new IllegalArgumentException();
        }
        List<Element> elements = new ArrayList<>();
        try {
            List<? extends ParserRuleContext> result = (List<? extends ParserRuleContext>)wrapped.getClass().getMethod(relation.getName()).invoke(wrapped);
            for (ParserRuleContext child : result) {
                elements.add(reflectionMapper.toElement(child));
            }
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return elements;
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
    public Optional<Object> getSingleProperty(Property property) {
        try {
            Object result = wrapped.getClass().getMethod(property.getName()).invoke(wrapped);
            if (result == null) {
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
