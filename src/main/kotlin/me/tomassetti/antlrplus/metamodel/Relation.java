package me.tomassetti.antlrplus.metamodel;

public class Relation extends Feature {
    public enum Type {
        CONTAINMENT,
        REFERENCE
    };

    private Type type;
    private Multiplicity multiplicity;
    private Entity source;

    public Type getType() {
        return type;
    }

    public boolean isSingle() {
        return multiplicity == Multiplicity.ONE;
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
                "name='" + getName() + '\'' +
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

        if (!getName().equals(relation.getName())) return false;
        if (type != relation.type) return false;
        if (multiplicity != relation.multiplicity) return false;
        if (!source.equals(relation.source)) return false;
        return target.equals(relation.target);

    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + multiplicity.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    private Entity target;

    public Relation(String name, Type type, Multiplicity multiplicity, Entity source, Entity target) {
        super(name);
        this.type = type;
        this.multiplicity = multiplicity;
        this.source = source;
        this.target = target;
    }
}
