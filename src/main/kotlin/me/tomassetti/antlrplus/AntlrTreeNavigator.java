package me.tomassetti.antlrplus;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Collections;
import java.util.Iterator;

public class AntlrTreeNavigator {

    private AntlrTreeNavigator() {

    }

    private static class MyIterator implements Iterator<ParseTree> {

        private ParserRuleContext parent;
        private int nextChildIndex;
        // it is null until we return the child
        private Iterator<ParseTree> currentChildIterator;

        public MyIterator(ParserRuleContext parent) {
            this.parent = parent;
            this.nextChildIndex = 0;
            this.currentChildIterator = null;
        }

        @Override
        public boolean hasNext() {
            if (nextChildIndex >= parent.getChildCount()) {
                return false;
            } else if (nextChildIndex < (parent.getChildCount() - 1) ) {
                return true;
            } else {
                // we are in the last child;
                if (currentChildIterator == null) {
                    // we have still to return the last child
                    return true;
                } else {
                    return currentChildIterator.hasNext();
                }
            }
        }

        @Override
        public ParseTree next() {
            if (currentChildIterator == null) {
                ParseTree child = parent.getChild(nextChildIndex);
                if (child instanceof ParserRuleContext) {
                    currentChildIterator = new MyIterator((ParserRuleContext) child);
                } else {
                    currentChildIterator = Collections.<ParseTree>emptyList().iterator();
                }
                return child;
            } else if (currentChildIterator.hasNext()){
                return currentChildIterator.next();
            } else {
                nextChildIndex++;
                currentChildIterator = null;
                return next();
            }
        }
    }

    public static Iterable<ParseTree> allChildrenIterable(final ParserRuleContext node) {
        return new Iterable<ParseTree>() {
            @Override
            public Iterator<ParseTree> iterator() {
                return new MyIterator(node);
            }
        };
    }

}
