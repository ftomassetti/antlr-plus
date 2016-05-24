package me.tomassetti.antlrplus.metamodel;

public class Relation {
    public enum Type {
        CONTAINMENT,
        REFERENCE
    };

    private String name;
    private Type type;
    private Multiplicity multiplicity;
    private Entity source;

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public Entity getSource() {
        return source;
    }

    public Entity getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", multiplicity=" + multiplicity +
                ", source=" + source.getName() +
                ", target=" + target.getName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relation)) return false;

        Relation relation = (Relation) o;

        if (!name.equals(relation.name)) return false;
        if (type != relation.type) return false;
        if (multiplicity != relation.multiplicity) return false;
        if (!source.equals(relation.source)) return false;
        return target.equals(relation.target);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + multiplicity.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    private Entity target;

    public Relation(String name, Type type, Multiplicity multiplicity, Entity source, Entity target) {
        this.name = name;
        this.type = type;
        this.multiplicity = multiplicity;
        this.source = source;
        this.target = target;
    }
}
