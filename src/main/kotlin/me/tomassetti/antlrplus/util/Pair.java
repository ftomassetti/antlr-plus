package me.tomassetti.antlrplus.util;

public class Pair<F, S> extends java.util.AbstractMap.SimpleImmutableEntry<F, S> {

    public  Pair( F f, S s ) {
        super( f, s );
    }

    public F getFirst() {
        return getKey();
    }

    public S getSecond() {
        return getValue();
    }

    public String toString() {
        return "["+getKey()+","+getValue()+"]";
    }

}
