package me.tomassetti.antlrplus.metamodel.mapping;

import me.tomassetti.antlrplus.metamodel.Entity;
import me.tomassetti.antlrplus.metamodel.Grammar;
import org.antlr.v4.runtime.ParserRuleContext;

public class ReflectionMapper {

    public <R extends ParserRuleContext> Entity getEntity(Class<R> ruleClass) {
        throw new UnsupportedOperationException();
    }

    public <R extends ParserRuleContext> Grammar getGrammar(String name, Class<R> rootRuleClass) {
        throw new UnsupportedOperationException();
    }
}
