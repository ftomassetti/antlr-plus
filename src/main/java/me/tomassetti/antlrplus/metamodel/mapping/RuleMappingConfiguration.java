package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.Property;

import java.util.HashSet;
import java.util.Set;

public class RuleMappingConfiguration {

    private Set<String> propertiesToIgnore = new HashSet<>();
    private Set<String> fieldsToConsider = new HashSet<>();

    public boolean canAdd(Property property) {
        return !propertiesToIgnore.contains(property.getName());
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
}
