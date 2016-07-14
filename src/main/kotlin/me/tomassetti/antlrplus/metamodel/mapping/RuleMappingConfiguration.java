package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.Property;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RuleMappingConfiguration {

    private Set<String> propertiesToIgnore = new HashSet<>();
    private Set<String> fieldsToConsider = new HashSet<>();
    private Map<String, String> featuressRenamedReversed = new HashMap<>();
    private Map<String, String> featuressRenamed = new HashMap<>();

    public boolean canAdd(Property property) {
        return !propertiesToIgnore.contains(property.getName());
    }

    public Map<String, String> getFeaturessRenamedReversed() {
        return featuressRenamedReversed;
    }

    public Map<String, String> getFeaturessRenamed() {
        return featuressRenamed;
    }

    public RuleMappingConfiguration ignoreProperty(String name) {
        propertiesToIgnore.add(name);
        return this;

    }

    public RuleMappingConfiguration considerField(String name) {
        fieldsToConsider.add(name);
        return this;
    }

    public Set<String> getFieldsToConsider() {
        return fieldsToConsider;
    }

    public String toReflectionName(String propertyName) {
        if (featuressRenamedReversed.containsKey(propertyName)) {
            return featuressRenamedReversed.get(propertyName);
        } else {
            return propertyName;
        }
    }

    public String fromReflectionName(String propertyName) {
        if (featuressRenamed.containsKey(propertyName)) {
            return featuressRenamed.get(propertyName);
        } else {
            return propertyName;
        }
    }

    public RuleMappingConfiguration renameFeature(String oldName, String newName) {
        featuressRenamedReversed.put(newName, oldName);
        featuressRenamed.put(oldName, newName);
        return this;
    }
}
