package eu.drus.jpa.unit.concordion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EqualsInterceptorTest {

    @Test
    public void testProxyEqualsTarget() throws Exception {
        // GIVEN
        final ClassA target = new ClassA();
        final Object proxy = EnhancedProxy.create(target, null);        

        // WHEN
        final boolean isEqual = (boolean) EqualsInterceptor.intercept(proxy, target, target);

        // THEN
        assertTrue(isEqual);
    }


    @Test
    public void testProxyEqualsSelf() throws Exception {
        // GIVEN
    	final ClassA target = new ClassA();
        final Object proxy = EnhancedProxy.create(target, null);

        // WHEN
        final boolean isEqual = (boolean) EqualsInterceptor.intercept(proxy, proxy, target);

        // THEN
        assertTrue(isEqual);
    }

    @Test
    public void testProxyEqualsOtherProxyForSameTarget() throws Exception {
        // GIVEN
        final ClassA target = new ClassA();
        final Object proxy1 = EnhancedProxy.create(target, null);
        
        final Object proxy2 = EnhancedProxy.create(target, null);

        // WHEN
        final boolean isEqual = (boolean) EqualsInterceptor.intercept(proxy1, proxy2, target);

        // THEN
        assertTrue(isEqual);
    }

    @Test
    public void testProxyForOneTargetIsNotEqualsToOtherTarget() throws Exception {
        // GIVEN
    	final ClassA target = new ClassA();
        final Object proxy = EnhancedProxy.create(target, null);

        // WHEN
        final boolean isEqual = (boolean) EqualsInterceptor.intercept(proxy, new ClassA(), target);

        // THEN
        assertFalse(isEqual);
    }

    @Test
    public void testProxyForOneTargetIsNotEqualsProxyForOtherTarget() throws Exception {
        // GIVEN
    	final ClassA target1 = new ClassA();
        final Object proxy1 = EnhancedProxy.create(target1, null);
        
        final ClassA target2 = new ClassA();
        final Object proxy2 = EnhancedProxy.create(target2, null);

        // WHEN
        final boolean isEqual = (boolean) EqualsInterceptor.intercept(proxy1, proxy2, target1);

        // THEN
        assertFalse(isEqual);
    }

    public static class ClassA {}
}
