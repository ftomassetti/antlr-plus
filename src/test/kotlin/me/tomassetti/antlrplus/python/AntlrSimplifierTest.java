package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.AntlrSimplifier;
import org.junit.Test;

public class AntlrSimplifierTest {

    @Test
    public void simplify() {
        AntlrSimplifier antlrSimplifier = new AntlrSimplifier();
        antlrSimplifier.simplify(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/python/Python3.g4"));
    }

}
