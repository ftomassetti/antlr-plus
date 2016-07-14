package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Property;
import me.tomassetti.antlrplus.metamodel.Relation;
import me.tomassetti.antlrplus.model.AbstractOrderedElement;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.model.OrderedElement;
import me.tomassetti.antlrplus.util.Pair;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class AntlrReflectionElement extends AbstractOrderedElement {

    private ParserRuleContext wrapped;
    private AntlrReflectionMapper reflectionMapper;
    private static final int EOF_TOKEN_TYPE = -1;

    protected Optional<Object> lookForCommonProperty(String name) {
        if (name.equals(AntlrReflectionMapper.START_LINE.getName())) {
            return Optional.of(this.wrapped.getStart().getLine());
        } else if (name.equals(AntlrReflectionMapper.END_LINE.getName())) {
            if (this.wrapped.getStop() == null) {
                if (this.wrapped.getStart() != null && this.wrapped.getText().isEmpty()) {
                    return Optional.of(this.wrapped.getStart().getCharPositionInLine());
                }
                throw new IllegalStateException("The node has no stop token. Wrapped class: "+wrapped.getClass().getCanonicalName()
                        +". SourceInterval: "+ this.wrapped.getSourceInterval().a+ " - "+this.wrapped.getSourceInterval().b+". Text: '"+this.wrapped.getText()+"'. Start token: "+this.wrapped.getStart());
            }
            return Optional.of(this.wrapped.getStop().getLine() + this.wrapped.getStop().getText().split("\n", -1).length - 1);
        } else if (name.equals(AntlrReflectionMapper.START_COLUMN.getName())) {
            return Optional.of(this.wrapped.getStart().getCharPositionInLine());
        } else if (name.equals(AntlrReflectionMapper.END_COLUMN.getName())) {
            if (this.wrapped.getStop() == null) {
                if (this.wrapped.getStart() != null && this.wrapped.getText().isEmpty()) {
                    return Optional.of(this.wrapped.getStart().getCharPositionInLine());
                }
                throw new IllegalStateException("The node has no stop token. Wrapped class: "+wrapped.getClass().getCanonicalName()
                        +". SourceInterval: "+ this.wrapped.getSourceInterval().a+ " - "+this.wrapped.getSourceInterval().b+". Text: '"+this.wrapped.getText()+"'. Start token: "+this.wrapped.getStart());
            }
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

    public void setFeaturesNameMapping(Map<String, String> featuresNameMapping) {
        this.featuresNameMapping = featuresNameMapping;
    }

    @Override
    public String toString() {
        return "ReflectionElement{" +
                "entity=" + entity +
                ", wrapped=" + wrapped +
                '}';

    }

    private Map<String, String> featuresNameMapping = new HashMap<>();

    public AntlrReflectionElement(AntlrReflectionMapper reflectionMapper, ParserRuleContext wrapped, Entity entity, Optional<OrderedElement> parent) {
        super(entity, parent);
        this.reflectionMapper = reflectionMapper;
        this.wrapped = wrapped;
    }

    private class TokenAdapter {
        private Token token;

        public TokenAdapter(Token token) {
            this.token = token;
        }

        public Interval getInterval() {
            int a = token.getStartIndex();
            int b = token.getStopIndex();
            return new Interval(a, b);
        }

        public String toString() {
            return token.getText();
        }
    }

    @Override
    public List<Object> getMultipleProperty(Property property) {
        if (property.isSingle()) {
            throw new IllegalArgumentException();
        }
        String name = toRawName(property.getName());
        List<Object> elements = new ArrayList<>();
        try {
            List<? extends Object> result = (List<? extends Object>) wrapped.getClass().getMethod(name).invoke(wrapped);

            // if it was obtained from a Rule to be treated as token we need to convert it
            //result = result.stream().map(e -> toToken(e)).collect(Collectors.toList());

            elements.addAll(result);
        } catch (NoSuchMethodException e) {
            try {
                List<? extends Object> result = (List<? extends Object>) wrapped.getClass().getField(name).get(wrapped);
                for (Object r : result) {
                    if (r instanceof Token) {
                        elements.add(new TokenAdapter((Token)r));
                    } else {
                        elements.add(r);
                    }
                }
            } catch (IllegalAccessException|NoSuchFieldException e1) {
                throw new RuntimeException(e1);
            }
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return elements;
    }

    private Optional<Field> getField(Class<?> clazz, String fieldName) {
        return Arrays.stream(clazz.getFields()).filter(f -> f.getName().equals(fieldName)).findFirst();
    }

    private String toRawName(String featureName) {
        if (this.featuresNameMapping.containsKey(featureName)) {
            return this.featuresNameMapping.get(featureName);
        } else {
            return featureName;
        }
    }

    private Optional<ParserRuleContext> getSingleRelationRaw(Relation relation) {
        if (!relation.isSingle()) {
            throw new IllegalArgumentException();
        }
        String name = toRawName(relation.getName());
        try {
            ParserRuleContext result;
            if (getField(wrapped.getClass(), name).isPresent()) {
                result = (ParserRuleContext) getField(wrapped.getClass(), name).get().get(wrapped);
            } else {
                result = (ParserRuleContext) wrapped.getClass().getMethod(name).invoke(wrapped);
            }
            if (result == null) {
                return Optional.empty();
            } else {
                if (reflectionMapper.isToBeDropped(result.getClass())) {
                    return Optional.empty();
                }
                return Optional.of(result);
            }
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassCastException e){
            throw new RuntimeException("Relation "+relation, e);
        }
    }

    @Override
    public Optional<Element> getSingleRelation(Relation relation) {
        try {
            Optional<ParserRuleContext> raw = getSingleRelationRaw(relation);
            return raw.map(e -> reflectionMapper.toElement(e, Optional.of(this)));
        } catch (RuntimeException e) {
            throw new RuntimeException("Issue accessing relation "+relation, e);
        }
    }

    private List<? extends ParserRuleContext> getMultipleRelationRaw(Relation relation) {
        if (relation.isSingle()) {
            throw new IllegalArgumentException();
        }
        String name = toRawName(relation.getName());
        try {
            List<? extends ParserRuleContext> result;
            if (getField(wrapped.getClass(), relation.getName()).isPresent()) {
                result = (List<? extends ParserRuleContext>) getField(wrapped.getClass(), name).get().get(wrapped);
            } else {
                result = (List<? extends ParserRuleContext>)wrapped.getClass().getMethod(name).invoke(wrapped);
            }
            result = result.stream().filter(e -> !reflectionMapper.isToBeDropped(e.getClass())).collect(Collectors.<ParserRuleContext>toList());
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
    public Optional<Object> getSingleProperty(Property property) {
        Optional<Object> res = lookForCommonProperty(property.getName());
        if (res.isPresent()) {
            return res;
        }
        String name = toRawName(property.getName());
        try {
            Object result = wrapped.getClass().getMethod(name).invoke(wrapped);
            if (result == null) {
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } catch (NoSuchMethodException e) {
            try {
                Object result = wrapped.getClass().getField(name).get(wrapped);
                if (result == null) {
                    return Optional.empty();
                } else {
                    if (result instanceof Token) {
                        return Optional.of(new TokenAdapter((Token)result));
                    }
                    return Optional.of(result);
                }
            } catch (IllegalAccessException|NoSuchFieldException e1) {
                throw new RuntimeException(e1);
            }
        } catch (IllegalAccessException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Interval toInterval(Object propertyValue) {
        if (propertyValue instanceof ParseTree) {
            return ((ParseTree) propertyValue).getSourceInterval();
        } else if (propertyValue instanceof TokenAdapter) {
            return ((TokenAdapter)propertyValue).getInterval();
        } else {
            //System.err.println("NO POSITION FOR "+propertyValue+ " "+propertyValue.getClass());
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
                //if (!AntlrReflectionMapper.POSITIONS_PROPERTIES.contains(o1.getFirst().getFeature())) {
                //    System.err.println("NO POSITIONS FOR " + o1.getKey());
                //}
                return -1;
                //return -1;
            }
            if (i2 == null) {
                //if (AntlrReflectionMapper.POSITIONS_PROPERTIES.contains(o2.getFirst().getFeature())) {
                //    System.err.println("NO POSITIONS FOR " + o2.getKey());
                //}
                return -1;
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
