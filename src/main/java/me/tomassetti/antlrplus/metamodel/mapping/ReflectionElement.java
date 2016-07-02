package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.model.OrderedElement;
import me.tomassetti.antlrplus.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

class ReflectionElement implements OrderedElement {

    private ParserRuleContext wrapped;
    private Entity entity;
    private ReflectionMapper reflectionMapper;
    private Optional<OrderedElement> parent;

    @Override
    public Optional<Element> getSingleRelation(String name) {
        return this.getSingleRelation(this.type().getRelation(name).get());
    }

    private static final int EOF_TOKEN_TYPE = -1;

    private Optional<Object> lookForCommonProperty(String name) {
        if (name.equals(ReflectionMapper.START_LINE.getName())) {
            return Optional.of(this.wrapped.getStart().getLine());
        } else if (name.equals(ReflectionMapper.END_LINE.getName())) {
            return Optional.of(this.wrapped.getStop().getLine() + this.wrapped.getStop().getText().split("\n", -1).length - 1);
        } else if (name.equals(ReflectionMapper.START_COLUMN.getName())) {
            return Optional.of(this.wrapped.getStart().getCharPositionInLine());
        } else if (name.equals(ReflectionMapper.END_COLUMN.getName())) {
            if (this.wrapped.getStop().getType() == EOF_TOKEN_TYPE) {
                return Optional.of(this.wrapped.getStop().getCharPositionInLine());
            }
            String[] lines = this.wrapped.getStop().getText().split("\n", -1);
            if (lines.length == 1) {
                return Optional.of(this.wrapped.getStop().getCharPositionInLine() + this.wrapped.getStop().getText().length());
            } else {
                return Optional.of(lines[lines.length - 1].length());
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Object> getSingleProperty(String name) {
        Optional<Object> res = lookForCommonProperty(name);
        if (res.isPresent()) {
            return res;
        }

        return this.getSingleProperty(this.type().getProperty(name).get());
    }

    @Override
    public String toString() {
        return "ReflectionElement{" +
                "entity=" + entity +
                ", wrapped=" + wrapped +
                '}';
    }

    @Override
    public Optional<Element> getParent() {
        if (parent.isPresent()) {
            return Optional.of(parent.get());
        } else {
            return Optional.empty();
        }
    }

    public ReflectionElement(ReflectionMapper reflectionMapper, ParserRuleContext wrapped, Entity entity, Optional<OrderedElement> parent) {
        this.reflectionMapper = reflectionMapper;
        this.wrapped = wrapped;
        this.entity = entity;

        this.parent = parent;
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

            // if it was obtained from a Rule to be treated as token we need to convert it
            //result = result.stream().map(e -> toToken(e)).collect(Collectors.toList());

            elements.addAll(result);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    private Optional<Field> getField(Class<?> clazz, String fieldName) {
        return Arrays.stream(clazz.getFields()).filter(f -> f.getName().equals(fieldName)).findFirst();
    }

    private Optional<ParserRuleContext> getSingleRelationRaw(Relation relation) {
        if (!relation.isSingle()) {
            throw new IllegalArgumentException();
        }
        try {
            ParserRuleContext result;
            if (getField(wrapped.getClass(), relation.getName()).isPresent()) {
                result = (ParserRuleContext) getField(wrapped.getClass(), relation.getName()).get().get(wrapped);
            } else {
                result = (ParserRuleContext) wrapped.getClass().getMethod(relation.getName()).invoke(wrapped);
            }
            if (result == null) {
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassCastException e){
            throw new RuntimeException("Relation "+relation, e);
        }
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
    public Optional<Element> getSingleRelation(Relation relation) {
        Optional<ParserRuleContext> raw = getSingleRelationRaw(relation);
        return raw.map(e -> reflectionMapper.toElement(e, Optional.of(this)));
    }

    @Override
    public void setSingleRelation(Relation relation, Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMultipleRelationCount(Relation relation) {
        throw new UnsupportedOperationException();
    }

    private List<? extends ParserRuleContext> getMultipleRelationRaw(Relation relation) {
        if (relation.isSingle()) {
            throw new IllegalArgumentException();
        }
        try {
            List<? extends ParserRuleContext> result;
            if (getField(wrapped.getClass(), relation.getName()).isPresent()) {
                result = (List<? extends ParserRuleContext>) getField(wrapped.getClass(), relation.getName()).get().get(wrapped);
            } else {
                result = (List<? extends ParserRuleContext>)wrapped.getClass().getMethod(relation.getName()).invoke(wrapped);
            }
            return result;
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Element> getMultipleRelation(Relation relation) {
        List<Element> elements = new ArrayList<>();
        for (ParserRuleContext child : getMultipleRelationRaw(relation)) {
            elements.add(reflectionMapper.toElement(child, Optional.of(this)));
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
        Optional<Object> res = lookForCommonProperty(property.getName());
        if (res.isPresent()) {
            return res;
        }
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

    private Interval toInterval(Object propertyValue) {
        if (propertyValue instanceof ParseTree) {
            return ((ParseTree) propertyValue).getSourceInterval();
        } else {
            return null;
        }
    }

    @Override
    public List<ValueReference> getValuesOrder() {
        List<Pair<ValueReference, Interval>> positions = new LinkedList<>();

        for (Relation relation : type().getRelations()) {
            if (relation.isSingle()) {
                Optional<ParserRuleContext> raw = getSingleRelationRaw(relation);
                if (raw.isPresent()) {
                    ValueReference vr = new ValueReference(relation, 0);
                    positions.add(new Pair<>(vr, raw.get().getSourceInterval()));
                }
            } else {
                List<? extends ParserRuleContext> raw = getMultipleRelationRaw(relation);
                for (int i=0;i<raw.size();i++) {
                    ValueReference vr = new ValueReference(relation, i);
                    positions.add(new Pair<>(vr, raw.get(i).getSourceInterval()));
                }
            }
        }
        for (Property property : type().getProperties()) {
            if (property.isSingle()) {
                Optional<Object> raw = getSingleProperty(property);
                if (raw.isPresent()) {
                    ValueReference vr = new ValueReference(property, 0);
                    positions.add(new Pair<>(vr, toInterval(raw.get())));
                }
            } else {
                List<Object> raw = getMultipleProperty(property);
                for (int i=0;i<raw.size();i++) {
                    ValueReference vr = new ValueReference(property, i);
                    positions.add(new Pair<>(vr, toInterval(raw.get(i))));
                }
            }
        }

        positions.sort((o1, o2) -> {
            Interval i1 = o1.getValue();
            Interval i2 = o2.getValue();
            if (i1 == null) {
                return -1;
            }
            if (i2 == null) {
                return 1;
            }
            if (i1.startsAfter(i2)) {
                return 1;
            } else if (i2.startsAfter(i1)){
                return -1;
            } else {
                return 0;
            }
        });
        return positions.stream().map(p -> p.getKey()).collect(Collectors.toList());
    }
}
