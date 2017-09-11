package eu.drus.jpa.unit.concordion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.cglib.proxy.Enhancer;

public class EqualsInterceptorTest {

    @Test
    public void testProxyEqualsTarget() {
        // GIVEN
        final ClassA target = new ClassA();
        final Object proxy = Enhancer.create(ClassA.class, new EqualsInterceptor(target));

        // WHEN
        final boolean isEqual = proxy.equals(target);

        // THEN
        assertTrue(isEqual);
    }

    @Test
    public void testProxyEqualsSelf() {
        // GIVEN
        final Object proxy = Enhancer.create(ClassA.class, new EqualsInterceptor(new ClassA()));

        // WHEN
        final boolean isEqual = proxy.equals(proxy);

        // THEN
        assertTrue(isEqual);
    }

    @Test
    public void testProxyEqualsOtherProxyForSameTarget() {
        // GIVEN
        final ClassA target = new ClassA();
        final Object proxy1 = Enhancer.create(ClassA.class, new EqualsInterceptor(target));
        final Object proxy2 = Enhancer.create(ClassA.class, new EqualsInterceptor(target));

        // WHEN
        final boolean isEqual = proxy1.equals(proxy2);

        // THEN
        assertTrue(isEqual);
    }

    @Test
    public void testProxyForOneTargetIsNotEqualsToOtherTarget() {
        // GIVEN
        final Object proxy = Enhancer.create(ClassA.class, new EqualsInterceptor(new ClassA()));

        // WHEN
        final boolean isEqual = proxy.equals(new ClassA());

        // THEN
        assertFalse(isEqual);
    }

    @Test
    public void testProxyForOneTargetIsNotEqualsProxyForOtherTarget() {
        // GIVEN
        final Object proxy1 = Enhancer.create(ClassA.class, new EqualsInterceptor(new ClassA()));
        final Object proxy2 = Enhancer.create(ClassA.class, new EqualsInterceptor(new ClassA()));

        // WHEN
        final boolean isEqual = proxy1.equals(proxy2);

        // THEN
        assertFalse(isEqual);
    }

    public static class ClassA {}
}
