package me.tomassetti.antlrplus.python;

import me.tomassetti.antlrplus.AntlrSimplifier;
import org.junit.Test;

/**
 * Created by federico on 28/05/16.
 */
public class AntlrSimplifierTest {

    @Test
    public void simplify() {
        AntlrSimplifier antlrSimplifier = new AntlrSimplifier();
        antlrSimplifier.simplify(this.getClass().getResourceAsStream("/me/tomassetti/antlrplus/java/Java8.g4"));
    }

}
