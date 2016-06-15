package me.tomassetti.antlrplus.model;

import me.tomassetti.antlrplus.metamodel.Feature;

import java.util.List;

/**
 * Element which has ordered values (both properties and relation values)
 */
public interface OrderedElement extends Element {

    class ValueReference {
        private Feature feature;
        private int index;

        public ValueReference(Feature feature, int index) {
            this.feature = feature;
            this.index = index;
        }

        public Feature getFeature() {
            return feature;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ValueReference)) return false;

            ValueReference that = (ValueReference) o;

            if (index != that.index) return false;
            return feature.equals(that.feature);
        }

        @Override
        public String toString() {
            return "ValueReference{" +
                    "feature=" + feature +
                    ", index=" + index +
                    '}';
        }

        @Override
        public int hashCode() {
            int result = feature.hashCode();
            result = 31 * result + index;
            return result;
        }
    }

    List<ValueReference> getValuesOrder();

}
