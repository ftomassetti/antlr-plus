package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.*;
import me.tomassetti.antlrplus.model.OrderedElement;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class AntlrReflectionMapper {

    private String[] ruleNames;
    private Class<? extends Lexer> lexerClass;

    public boolean addPositions = false;

    public boolean isToBeDropped(Class<? extends ParserRuleContext> ruleClass) {
        return this.rulesToDrop.contains(ruleClass);
    }

    public boolean isAddPositions() {
        return addPositions;
    }

    private Map<Class<? extends ParserRuleContext>, RuleMappingConfiguration> ruleConfigurations = new HashMap<>();

    public void setRuleConfiguration(Class<? extends ParserRuleContext> rule, RuleMappingConfiguration configuration) {
        ruleConfigurations.put(rule, configuration);
    }

    private RuleMappingConfiguration getRuleMappingConfiguration(Class<? extends ParserRuleContext> rule) {
        if (!ruleConfigurations.containsKey(rule)) {
            setRuleConfiguration(rule, new RuleMappingConfiguration());
        }
        return ruleConfigurations.get(rule);
    }

    public void setAddPositions(boolean addPositions) {
        this.addPositions = addPositions;
    }

    private Map<String, Entity> classesToEntities = new HashMap<>();


    public Set<Entity> allKnownEntities() {
        Set<Entity> entities = new HashSet<>();
        entities.addAll(this.classesToEntities.values());
        return entities;
    }

    public AntlrReflectionMapper(String[] ruleNames, Class<? extends Lexer> lexerClass) {
        this.ruleNames = ruleNames;
        this.lexerClass = lexerClass;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    private boolean debug = false;

    public void debugMsg(String msg) {
        if (debug) {
            System.out.println("ARM DEBUG " + msg);
        }
    }

    private static Set<String> methodNamesToIgnore = new HashSet<>(Arrays.asList("enterRule", "exitRule", "getRuleIndex"));

    private Set<Class<? extends ParserRuleContext>> transparentEntities = new HashSet<>();
    private Set<Class<? extends ParserRuleContext>> toTreatAsToken = new HashSet<>();
    private Set<String> tokensToIgnore = new HashSet<>();
    private Set<Integer> typesOfTokensToIgnore = new HashSet<>();
    private Map<Class<? extends ParserRuleContext>, String> entitiesNames = new HashMap<>();

    public void setEntityName(Class<? extends ParserRuleContext> ruleClass, String entityName) {
        entitiesNames.put(ruleClass, entityName);
    }

    public void markAsTransparent(Class<? extends ParserRuleContext> ruleClass) {
        transparentEntities.add(ruleClass);
    }

    public void markAsTokenToIgnore(String token) {
        tokensToIgnore.add(token);
        try {
            if (token.equals("EOF")) {
                typesOfTokensToIgnore.add(-1);
            } else {
                int type = (Integer) lexerClass.getDeclaredField(token).get(null);
                typesOfTokensToIgnore.add(type);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public void markAsTreatAsToken(Class<? extends ParserRuleContext> ruleClass) {
        toTreatAsToken.add(ruleClass);
    }

    public Entity getEntity(Class<? extends ParserRuleContext> ruleClass) {
        if (transparentEntities.contains(ruleClass)) {
            throw new IllegalArgumentException("Transparent rule, no corresponding Entity can be generated for "+ruleClass);
        }
        if (toTreatAsToken.contains(ruleClass)) {
            throw new IllegalArgumentException("Rule to be treated like a token, no corresponding Entity can be generated for "+ruleClass);
        }
        if (rulesToDrop.contains(ruleClass)) {
            throw new IllegalArgumentException("Rule to be dropped, no corresponding Entity can be generated for "+ruleClass);
        }
        if (!classesToEntities.containsKey(ruleClass.getCanonicalName())) {
            registerEntity(ruleClass);
        }
        return classesToEntities.get(ruleClass.getCanonicalName());
    }

    // Visible for testing
    static boolean isListOf(Type listType, Class<?> elementType) {
        if (!(listType instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType parameterizedType = (ParameterizedType)listType;
        if (!parameterizedType.getRawType().getTypeName().equals(List.class.getCanonicalName())) {
            return false;
        } else {
            return parameterizedType.getActualTypeArguments()[0].getTypeName().replaceAll("\\$", ".").equals(elementType.getCanonicalName());
        }
    }

    // Visible for testing
    static List<Field> fieldsOfType(Class<? extends ParserRuleContext> ruleClass, Class<? extends ParserRuleContext> childType) {
        List<Field> result = Arrays.stream(ruleClass.getDeclaredFields()).filter(f -> (f.getType().equals(childType)) || (isListOf(f.getGenericType(), childType)))
                .collect(Collectors.toList());
        return result;
    }

    private static List<Field> notShadowedFieldsOfType(Class<? extends ParserRuleContext> ruleClass, Class<? extends ParserRuleContext> childType) {
        List<Field> fields = fieldsOfType(ruleClass, childType);
        // a field is shadowed if it is named as the type and there are other fields containing a List of that type
        if (fields.size() > 1) {
            List<Field> filtered = new LinkedList<>();
            for (Field field : fields) {
                boolean shadowed = false;
                if (!(field.getGenericType() instanceof ParameterizedType)) {
                    String[] nameParts = field.getGenericType().getTypeName().replaceAll("\\$", ".").split("\\.");
                    String simpleName = nameParts[nameParts.length - 1];
                    simpleName = simpleName.substring(0, simpleName.length() - "Context".length());
                    if (simpleName.length() == 1) {
                        simpleName = simpleName.toLowerCase();
                    } else {
                        simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
                    }
                    shadowed = simpleName.equals(field.getName());
                }
                if (!shadowed) {
                    filtered.add(field);
                }
            }
            return filtered;
        } else {
            return fields;
        }
    }

    public static final Property START_LINE = new Property("startLine", Property.Datatype.INTEGER, Multiplicity.ONE);
    public static final Property END_LINE = new Property("endLine", Property.Datatype.INTEGER, Multiplicity.ONE);
    public static final Property START_COLUMN = new Property("startColumn", Property.Datatype.INTEGER, Multiplicity.ONE);
    public static final Property END_COLUMN = new Property("endColumn", Property.Datatype.INTEGER, Multiplicity.ONE);

    private Set<Class<? extends ParserRuleContext>> rulesToDrop = new HashSet<>();

    public void addToRulesToDrop(Class<? extends ParserRuleContext> ruleClass) {
        rulesToDrop.add(ruleClass);
    }

    protected void postProcessEntity(Entity entity) {

    }

    private String entityName(Class<? extends ParserRuleContext> ruleClass) {
        if (entitiesNames.containsKey(ruleClass)) {
            return entitiesNames.get(ruleClass);
        }
        String name = ruleClass.getSimpleName();
        if (!name.endsWith("Context")) {
            throw new RuntimeException("Name is not ending in Context: "+name);
        }
        return name.substring(0, name.length() - "Context".length());
    }

    public boolean isTransparent(Class<? extends ParserRuleContext> ruleClass) {
        return transparentEntities.contains(ruleClass);
    }

    public void registerWholeParser(Class<? extends Parser> parserClass) {
        for (Class c : parserClass.getDeclaredClasses()) {
            if (ParserRuleContext.class.isAssignableFrom(c)) {
                if (!this.isTransparent(c)
                        && !this.toTreatAsToken.contains(c)
                        && !this.rulesToDrop.contains(c)) {
                    // this force the registration
                    this.getEntity(c);
                }
            }
        }
    }

    private void registerEntity(Class<? extends ParserRuleContext> ruleClass) {
        RuleMappingConfiguration ruleConfiguration = getRuleMappingConfiguration(ruleClass);

        debugMsg("(Start considering class "+ruleClass.getName()+")");
        Entity entity = null;

        if (!ruleClass.getSuperclass().getCanonicalName().equals(ParserRuleContext.class.getCanonicalName())) {
            Entity parent = getEntity((Class<? extends ParserRuleContext>) ruleClass.getSuperclass());
            parent.setAbstract(true);
            entity = new Entity(entityName(ruleClass), parent);
        } else {
            entity = new Entity(entityName(ruleClass));
        }

        if (this.addPositions) {
            entity.addProperty(START_LINE);
            entity.addProperty(END_LINE);
            entity.addProperty(START_COLUMN);
            entity.addProperty(END_COLUMN);
        }

        // store immediately: so we can support recursive references
        classesToEntities.put(ruleClass.getCanonicalName(), entity);

        for (Method method : ruleClass.getDeclaredMethods()) {
            if (!methodNamesToIgnore.contains(method.getName()) && method.getParameterCount() == 0) {
                debugMsg("Considering method "+method.getName());
                if (method.getReturnType().getCanonicalName().equals(List.class.getCanonicalName())) {
                    debugMsg(" Method returning a list");
                    ParameterizedType listType = (ParameterizedType)method.getGenericReturnType();
                    Class elementType = (Class) listType.getActualTypeArguments()[0];
                    if (elementType.getCanonicalName().equals(TerminalNode.class.getCanonicalName())) {
                        debugMsg("  Method returning a list of terminals");
                        // for now we look at the method name to recognize the properties to ignore
                        if (!tokensToIgnore.contains(method.getName())) {
                            Property property = new Property(method.getName(), Property.Datatype.STRING, Multiplicity.MANY);
                            if (ruleConfiguration.canAdd(property)) {
                                debugMsg("   Adding property " + property);
                                entity.addProperty(property);
                            }
                        }
                    } else {
                        Class<? extends ParserRuleContext> childType = (Class<? extends ParserRuleContext>)elementType;
                        debugMsg("  Method returning a list of non-terminals of type "+ childType);
                        if (notShadowedFieldsOfType(ruleClass, childType).isEmpty()) {
                            debugMsg("   All fields shadowed");
                            Class<? extends ParserRuleContext> effectiveChildType = skipTransparentClasses(childType);
                            if (toTreatAsToken.contains(effectiveChildType)) {
                                Property property = new Property(method.getName(), Property.Datatype.STRING, Multiplicity.MANY);
                                if (ruleConfiguration.canAdd(property)) {
                                    debugMsg("    Adding property " + property);
                                    entity.addProperty(property);
                                }
                            } else {
                                if (!rulesToDrop.contains(effectiveChildType)) {
                                    Entity target = getEntity(effectiveChildType);
                                    Relation relation = new Relation(method.getName(),
                                            Relation.Type.CONTAINMENT,
                                            Multiplicity.MANY,
                                            entity,
                                            target);
                                    debugMsg("    Adding relation " + relation);
                                    entity.addRelation(relation);
                                }
                            }
                        } else {
                            debugMsg("   Not all fields shadowed");
                            for (Field f : notShadowedFieldsOfType(ruleClass, childType)) {
                                Class<? extends ParserRuleContext> effectiveChildType = skipTransparentClasses(childType);
                                if (toTreatAsToken.contains(effectiveChildType)) {
                                    Property property = new Property(f.getName(), Property.Datatype.STRING, f.getType().getCanonicalName().equals(List.class.getCanonicalName()) ? Multiplicity.MANY : Multiplicity.ONE);
                                    if (ruleConfiguration.canAdd(property)) {
                                        debugMsg("    Adding property " + property + " from field " + f);
                                        entity.addProperty(property);
                                    }
                                } else {
                                    if (!rulesToDrop.contains(effectiveChildType)) {
                                        Entity target = getEntity(effectiveChildType);
                                        Relation relation = new Relation(f.getName(),
                                                Relation.Type.CONTAINMENT,
                                                f.getType().getCanonicalName().equals(List.class.getCanonicalName()) ? Multiplicity.MANY : Multiplicity.ONE,
                                                entity,
                                                target);
                                        debugMsg("    Adding relation " + relation + " from field " + f);
                                        entity.addRelation(relation);
                                    }
                                }
                            }
                        }
                    }
                } else if (method.getReturnType().getCanonicalName().equals(TerminalNode.class.getCanonicalName())) {
                    debugMsg("  Method returning a single terminal");
                    // for now we look at the method name to recognize the properties to ignore
                    if (!tokensToIgnore.contains(method.getName())) {
                        Property property = new Property(method.getName(), Property.Datatype.STRING, Multiplicity.ONE);
                        if (ruleConfiguration.canAdd(property)) {
                            debugMsg("   Adding property " + property);
                            entity.addProperty(property);
                        }
                    }
                } else {
                    Class<? extends ParserRuleContext> childType = (Class<? extends ParserRuleContext>)method.getReturnType();
                    debugMsg("  Method returning a single non-terminal "+childType);
                    if (notShadowedFieldsOfType(ruleClass, childType).isEmpty()) {
                        Class<? extends ParserRuleContext> effectiveChildType = skipTransparentClasses(childType);
                        if (toTreatAsToken.contains(effectiveChildType)) {
                            Property property = new Property(method.getName(), Property.Datatype.STRING, Multiplicity.ONE);
                            if (ruleConfiguration.canAdd(property)) {
                                debugMsg("   Adding property " + property);
                                entity.addProperty(property);
                            }
                        } else {
                            if (!rulesToDrop.contains(effectiveChildType)) {
                                Entity target = getEntity(effectiveChildType);
                                Relation relation = new Relation(method.getName(),
                                        Relation.Type.CONTAINMENT,
                                        Multiplicity.ONE,
                                        entity,
                                        target);
                                debugMsg("   Adding relation " + relation);
                                entity.addRelation(relation);
                            }
                        }
                    } else {
                        for (Field f : notShadowedFieldsOfType(ruleClass, childType)) {
                            Class<? extends ParserRuleContext> effectiveChildType = skipTransparentClasses(childType);
                            if (toTreatAsToken.contains(effectiveChildType)) {
                                Property property = new Property(f.getName(), Property.Datatype.STRING, f.getType().getCanonicalName().equals(List.class.getCanonicalName()) ? Multiplicity.MANY : Multiplicity.ONE);
                                if (ruleConfiguration.canAdd(property)) {
                                    debugMsg("   Adding property " + property + " from field " + f);
                                    entity.addProperty(property);
                                }
                            } else {
                                if (!rulesToDrop.contains(effectiveChildType)) {
                                    Entity target = getEntity(effectiveChildType);
                                    Relation relation = new Relation(f.getName(),
                                            Relation.Type.CONTAINMENT,
                                            f.getType().getCanonicalName().equals(List.class.getCanonicalName()) ? Multiplicity.MANY : Multiplicity.ONE,
                                            entity,
                                            target);
                                    debugMsg("   Adding relation " + relation + " from field " + f);
                                    entity.addRelation(relation);
                                }
                            }
                        }
                    }
                }
            }
        }
        for (String fieldName : ruleConfiguration.getFieldsToConsider()) {
            try {
                Field field = ruleClass.getDeclaredField(fieldName);
                if (field.getType().getCanonicalName().equals(List.class.getCanonicalName())) {
                    ParameterizedType listType = (ParameterizedType)field.getGenericType();
                    Class elementType = (Class) listType.getActualTypeArguments()[0];
                    if (elementType.getCanonicalName().equals(TerminalNode.class.getCanonicalName()) || elementType.getCanonicalName().equals(Token.class.getCanonicalName())) {
                        Property property = new Property(fieldName, Property.Datatype.STRING, Multiplicity.MANY);
                        debugMsg("Adding property because of field " + property);
                        entity.addProperty(property);
                    } else {
                        Class<? extends ParserRuleContext> effectiveChildType = skipTransparentClasses(elementType);
                        if (toTreatAsToken.contains(effectiveChildType)) {
                            Property property = new Property(fieldName, Property.Datatype.STRING, Multiplicity.MANY);
                            if (ruleConfiguration.canAdd(property)) {
                                debugMsg("Adding property " + property + " from field " + field);
                                entity.addProperty(property);
                            }
                        } else {
                            Relation relation = new Relation(fieldName, Relation.Type.CONTAINMENT, Multiplicity.MANY, entity, getEntity(effectiveChildType));
                            debugMsg("Adding relation because of field " + relation);
                            entity.addRelation(relation);
                        }
                    }
                } else if (field.getType().getCanonicalName().equals(TerminalNode.class.getCanonicalName()) || field.getType().getCanonicalName().equals(Token.class.getCanonicalName())) {
                    Property property = new Property(fieldName, Property.Datatype.STRING, Multiplicity.ONE);
                    debugMsg("Adding property because of field " + property);
                    entity.addProperty(property);
                } else {
                    Class<? extends ParserRuleContext> effectiveChildType = skipTransparentClasses((Class<? extends ParserRuleContext>) field.getType());
                    if (toTreatAsToken.contains(effectiveChildType)) {
                        Property property = new Property(fieldName, Property.Datatype.STRING, Multiplicity.ONE);
                        if (ruleConfiguration.canAdd(property)) {
                            debugMsg("Adding property " + property + " from field " + field);
                            entity.addProperty(property);
                        }
                    } else {
                        if (!rulesToDrop.contains(effectiveChildType)) {
                            Relation relation = new Relation(fieldName, Relation.Type.CONTAINMENT, Multiplicity.ONE, entity, getEntity(effectiveChildType));
                            debugMsg("Adding relation because of field " + relation);
                            entity.addRelation(relation);
                        }
                    }
                }
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        debugMsg("(End considering class "+ruleClass.getName()+")");
        postProcessEntity(entity);
    }

    private Class<? extends ParserRuleContext> getOnlySingleRelation(Class<? extends ParserRuleContext> ruleClass) {
        for (Method method : ruleClass.getDeclaredMethods()) {
            if (!methodNamesToIgnore.contains(method.getName()) && method.getParameterCount() == 0) {
                if (method.getReturnType().getCanonicalName().equals(List.class.getCanonicalName())) {
                    throw new IllegalArgumentException("This class was expected to have one single relation: " + ruleClass);
                } else if (method.getReturnType().getCanonicalName().equals(TerminalNode.class.getCanonicalName())) {
                    throw new IllegalArgumentException("This class was expected to have one single relation: " + ruleClass);
                } else {
                    return (Class<? extends ParserRuleContext>) method.getReturnType();
                }
            }
        }
        throw new IllegalArgumentException("This class was expected to have one single relation: " + ruleClass);
    }

    private Class<? extends ParserRuleContext> skipTransparentClasses(Class<? extends ParserRuleContext> ruleClass) {
        if (transparentEntities.contains(ruleClass)) {
            return skipTransparentClasses(getOnlySingleRelation(ruleClass));
        } else {
            return ruleClass;
        }
    }

    public <R extends ParserRuleContext> Grammar getGrammar(String name, Class<R> rootRuleClass) {
        throw new UnsupportedOperationException();
    }

    private boolean isTerminalToBeIgnored(ParseTree parseTree) {
        if (parseTree instanceof TerminalNode) {
            TerminalNode terminalNode = (TerminalNode)parseTree;
            return this.typesOfTokensToIgnore.contains(terminalNode.getSymbol().getType());
        } else {
            return false;
        }
    }

    private List<ParseTree> relevantChildren(ParserRuleContext astNode) {
        List<ParseTree> children = new LinkedList<>();
        for (int i=0;i<astNode.getChildCount();i++) {
            ParseTree child = astNode.getChild(i);
            if (!isTerminalToBeIgnored(child)) {
                children.add(child);
            }
        }
        return children;
    }

    public OrderedElement toRootElement(ParserRuleContext astNode) {
        return toElement(astNode, Optional.empty());
    }

    public OrderedElement toElement(ParserRuleContext astNode, Optional<OrderedElement> parent) {
        if (transparentEntities.contains(astNode.getClass())) {
            List<ParseTree> children = relevantChildren(astNode);
            if (children.size() != 1) {
                throw new IllegalArgumentException("Transparent rules are expected to have exactly one child: " + astNode.getClass() + " while it has " + children.size());
            }
            if (!(children.get(0) instanceof ParserRuleContext)) {
                throw new IllegalArgumentException("A transparent rule only child is expected to be a non-terminal: " + astNode.getClass());
            }
            return toElement((ParserRuleContext) children.get(0), parent);
        }
        return instantiateElement(astNode, parent);
    }

    protected OrderedElement instantiateElement(ParserRuleContext astNode, Optional<OrderedElement> parent) {
        return new AntlrReflectionElement(this, astNode, getEntity(astNode.getClass()), parent);
    }
}
