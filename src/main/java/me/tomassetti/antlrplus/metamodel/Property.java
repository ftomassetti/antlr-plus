package me.tomassetti.antlrplus.metamodel;

public class Property extends Feature {
    public enum Datatype {
        STRING,
        INTEGER,
        BOOLEAN
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + getName() + '\'' +
                ", datatype=" + datatype +
                ", multiplicity=" + multiplicity +
                '}';
    }

    public boolean isSingle() {
        return multiplicity == Multiplicity.ONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Property)) return false;

        Property property = (Property) o;

        if (!getName().equals(property.getName())) return false;
        if (datatype != property.datatype) return false;
        return multiplicity == property.multiplicity;

    }

    public Datatype getDatatype() {
        return datatype;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    @Override

    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + datatype.hashCode();
        result = 31 * result + multiplicity.hashCode();
        return result;
    }

    private Datatype datatype;
    private Multiplicity multiplicity;

    public Property(String name, Datatype datatype, Multiplicity multiplicity) {
        super(name);
        this.datatype = datatype;
        this.multiplicity = multiplicity;
    }
}
