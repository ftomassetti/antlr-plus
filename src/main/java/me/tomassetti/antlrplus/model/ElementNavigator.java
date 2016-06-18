package me.tomassetti.antlrplus.model;

import me.tomassetti.antlrplus.metamodel.Entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ElementNavigator {

    public static List<OrderedElement> allDescendants(OrderedElement root, boolean includingItself) {
        List<OrderedElement> res = new LinkedList<>();
        if (includingItself) {
            res.add(root);
        }
        for (Element child: root.getAllChildren()) {
            res.addAll(ElementNavigator.allDescendants((OrderedElement) child, true));
        }
        return res;
    }

    public static Optional<OrderedElement> findAncestor(OrderedElement element, Entity parentType) {
        if (element.getParent().isPresent()) {
            if (element.getParent().get().type().equals(parentType)) {
                return Optional.of((OrderedElement) element.getParent().get());
            } else {
                return findAncestor((OrderedElement) element.getParent().get(), parentType);
            }
        } else {
            return Optional.empty();
        }
    }

}
