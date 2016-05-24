package me.tomassetti.antlrplus.metamodel;

import java.util.LinkedList;
import java.util.List;

public class Grammar {

    private List<Entity> entities;
    private Entity rootEntity;
    private String name;

    public Grammar(String name, Entity rootEntity) {
        this.name = name;
        this.rootEntity = rootEntity;
        this.entities = new LinkedList<>();
        this.entities.add(rootEntity);
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }
}
