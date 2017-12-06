package tests;

import org.junit.After;
import org.junit.Before;

public class AbstractLocaleTest {
    private java.util.Locale original;

    @Before
    public void before() {
        original = java.util.Locale.getDefault();
    }

    @After
    public void after() {
        java.util.Locale.setDefault(original);
    }
}
