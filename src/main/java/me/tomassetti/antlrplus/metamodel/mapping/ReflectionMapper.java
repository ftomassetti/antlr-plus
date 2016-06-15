package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.*;
import me.tomassetti.antlrplus.model.Element;
import me.tomassetti.antlrplus.model.OrderedElement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ReflectionMapper {

    private String[] ruleNames;

    private Map<String, Entity> classesToEntities = new HashMap<>();

    public ReflectionMapper(String[] ruleNames) {
        this.ruleNames = ruleNames;
    }

    private static Set<String> methodNamesToIgnore = new HashSet<>(Arrays.asList("enterRule", "exitRule", "getRuleIndex"));

    private Set<Class<? extends ParserRuleContext>> transparentEntities = new HashSet<>();

    public void markAsTransparent(Class<? extends ParserRuleContext> ruleClass) {
        transparentEntities.add(ruleClass);
    }

    public Entity getEntity(Class<? extends ParserRuleContext> ruleClass) {
        if (transparentEntities.contains(ruleClass)) {
            throw new IllegalArgumentException("Transparent rule, no corresponding Entity can be generated");
        }
        if (!classesToEntities.containsKey(ruleClass.getCanonicalName())) {
            registerEntity(ruleClass);
        }
        return classesToEntities.get(ruleClass.getCanonicalName());
    }

    private void registerEntity(Class<? extends ParserRuleContext> ruleClass) {
        String name = ruleClass.getSimpleName();
        if (!name.endsWith("Context")) {
            throw new RuntimeException("Name is not ending in Context: "+name);
        }
        Entity entity = new Entity(name.substring(0, name.length() - "Context".length()));
        // store immediately: so we can support recursive references
        classesToEntities.put(ruleClass.getCanonicalName(), entity);

        for (Method method : ruleClass.getDeclaredMethods()) {
            if (!methodNamesToIgnore.contains(method.getName()) && method.getParameterCount() == 0) {
                if (method.getReturnType().getCanonicalName().equals(List.class.getCanonicalName())) {
                    ParameterizedType listType = (ParameterizedType)method.getGenericReturnType();
                    Class elementType = (Class) listType.getActualTypeArguments()[0];
                    if (elementType.getCanonicalName().equals(TerminalNode.class.getCanonicalName())) {
                        Property property = new Property(method.getName(), Property.Datatype.STRING, Multiplicity.MANY);
                        entity.addProperty(property);
                    } else {
                        Entity target = getEntity((Class<? extends ParserRuleContext>) elementType);
                        Relation relation = new Relation(method.getName(),
                                Relation.Type.CONTAINMENT,
                                Multiplicity.MANY,
                                entity,
                                target);
                        entity.addRelation(relation);
                    }
                } else if (method.getReturnType().getCanonicalName().equals(TerminalNode.class.getCanonicalName())) {
                    Property property = new Property(method.getName(), Property.Datatype.STRING, Multiplicity.ONE);
                    entity.addProperty(property);
                } else {
                    Entity target = getEntity((Class<? extends ParserRuleContext>) method.getReturnType());
                    Relation relation = new Relation(method.getName(),
                            Relation.Type.CONTAINMENT,
                            Multiplicity.ONE,
                            entity,
                            target);
                    entity.addRelation(relation);
                }
            }
        }
    }

    public <R extends ParserRuleContext> Grammar getGrammar(String name, Class<R> rootRuleClass) {
        throw new UnsupportedOperationException();
    }

    public OrderedElement toElement(ParserRuleContext astNode) {
        if (transparentEntities.contains(astNode.getClass())) {
            if (astNode.getChildCount() != 1) {
                throw new IllegalArgumentException("Transparent rules are expected to have exactly one child: " + astNode.getClass());
            }
            if (!(astNode.getChild(0) instanceof ParserRuleContext)) {
                throw new IllegalArgumentException("A transparent rule only child is expected to be a non-terminal: " + astNode.getClass());
            }
            return toElement((ParserRuleContext) astNode.getChild(0));
        }
        return new ReflectionElement(this, astNode, getEntity(astNode.getClass()));
    }
}
