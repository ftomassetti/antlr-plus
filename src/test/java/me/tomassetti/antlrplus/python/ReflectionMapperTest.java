package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.mapping.ReflectionMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReflectionMapperTest {

    @Test
    public void getSimpleEntity() {
        Entity entity = new ReflectionMapper().getEntity(Python3Parser.Pass_stmtContext.class);

    }
}
