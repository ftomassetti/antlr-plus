package me.tomassetti.antlrplus.metamodel;

public class Property {
    public enum Datatype {
        STRING,
        INTEGER,
        BOOLEAN
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
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

        if (!name.equals(property.name)) return false;
        if (datatype != property.datatype) return false;
        return multiplicity == property.multiplicity;

    }

    public String getName() {
        return name;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    @Override

    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + datatype.hashCode();
        result = 31 * result + multiplicity.hashCode();
        return result;
    }

    private String name;
    private Datatype datatype;
    private Multiplicity multiplicity;

    public Property(String name, Datatype datatype, Multiplicity multiplicity) {
        this.name = name;
        this.datatype = datatype;
        this.multiplicity = multiplicity;
    }
}
