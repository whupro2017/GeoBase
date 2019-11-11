package whu.cast;

import org.junit.Test;

public class GeneralCastTest {
    @Test public void CastDoubleToInteger() {
        double d = 1.232;
        int i = (int) d;
        assert (i == 1);
    }
}
