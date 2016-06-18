package me.tomassetti.antlrplus.model;

import me.tomassetti.antlrplus.metamodel.Entity;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ElementNavigator {

    public static List<Element> allDescendants(Element root, boolean includingItself) {
        List<Element> res = new LinkedList<>();
        if (includingItself) {
            res.add(root);
        }
        for (Element child: root.getAllChildren()) {
            res.addAll(ElementNavigator.allDescendants(child, true));
        }
        return res;
    }

    public static Optional<Element> findAncestor(Element element, Entity parentType) {
        if (element.getParent().isPresent()) {
            if (element.getParent().get().type().equals(parentType)) {
                return Optional.of(element.getParent().get());
            } else {
                return findAncestor(element.getParent().get(), parentType);
            }
        } else {
            return Optional.empty();
        }
    }

}
