package eu.drus.jpa.unit.concordion;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.TestInvocation;
import net.sf.cglib.proxy.MethodProxy;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JpaUnitContext.class)
public class ConcordionInterceptorTest {

    public static class TestDelegate {}

    private static final Integer RESULT = Integer.MIN_VALUE;

    @Mock
    private JpaUnitContext context;

    @Mock
    private MethodProxy methodProxy;

    @Mock
    private DecoratorExecutor executor;

    private Method method;

    private TestDelegate delegate;

    private ConcordionInterceptor interceptor;

    @Before
    public void prepareTest() throws Throwable {
        mockStatic(JpaUnitContext.class);
        when(JpaUnitContext.getInstance(any(Class.class))).thenReturn(context);

        method = getClass().getDeclaredMethod("prepareTest");
        delegate = new TestDelegate();

        interceptor = new ConcordionInterceptor(executor, delegate);
    }

    @Test
    public void testInterceptMethodInvocationWithoutExceptionThrown() throws Throwable {
        // GIVEN
        when(methodProxy.invoke(any(), any(Object[].class))).thenReturn(RESULT);
        final Object[] args = new Object[0];

        // WHEN
        final Object res = interceptor.intercept(null, method, args, methodProxy);

        // THEN
        assertThat(res, equalTo(RESULT));

        final ArgumentCaptor<TestInvocation> beforeCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        final ArgumentCaptor<TestInvocation> afterCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        final InOrder inOrder = inOrder(executor, methodProxy);
        inOrder.verify(executor).processBefore(beforeCaptor.capture());
        inOrder.verify(methodProxy).invoke(any(), any(Object[].class));
        inOrder.verify(executor).processAfter(afterCaptor.capture());

        final TestInvocation invocation1 = beforeCaptor.getValue();
        final TestInvocation invocation2 = afterCaptor.getValue();

        assertThat(invocation1, equalTo(invocation2));
        assertThat(invocation1.getException().isPresent(), equalTo(Boolean.FALSE));

        assertThat(invocation1.getContext(), equalTo(context));
        assertThat(invocation1.getFeatureResolver().shouldCleanupAfter(), equalTo(Boolean.FALSE));
        assertThat(invocation1.getFeatureResolver().shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(invocation1.getTestClass(), equalTo(TestDelegate.class));
        assertThat(invocation1.getTestInstance().get(), equalTo(delegate));
        assertThat(invocation1.getTestMethod().get(), equalTo(method));
    }

    @Test
    public void testInterceptMethodInvocationWithExceptionThrown() throws Throwable {
        // GIVEN
        final JpaUnitException exception = new JpaUnitException("some reason");
        when(methodProxy.invoke(any(), any(Object[].class))).thenThrow(exception);
        final Object[] args = new Object[0];

        // WHEN
        try {
            interceptor.intercept(null, method, args, methodProxy);
            fail("JpaUnitException expected");
        } catch (final JpaUnitException e) {
            assertThat(e, equalTo(exception));
        }

        // THEN
        final ArgumentCaptor<TestInvocation> beforeCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        final ArgumentCaptor<TestInvocation> afterCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        final InOrder inOrder = inOrder(executor, methodProxy);
        inOrder.verify(executor).processBefore(beforeCaptor.capture());
        inOrder.verify(methodProxy).invoke(any(), any(Object[].class));
        inOrder.verify(executor).processAfter(afterCaptor.capture());

        final TestInvocation invocation1 = beforeCaptor.getValue();
        final TestInvocation invocation2 = afterCaptor.getValue();

        assertThat(invocation1, equalTo(invocation2));
        assertThat(invocation1.getException().isPresent(), equalTo(Boolean.TRUE));

        assertThat(invocation1.getContext(), equalTo(context));
        assertThat(invocation1.getFeatureResolver().shouldCleanupAfter(), equalTo(Boolean.FALSE));
        assertThat(invocation1.getFeatureResolver().shouldCleanupBefore(), equalTo(Boolean.FALSE));
        assertThat(invocation1.getTestClass(), equalTo(TestDelegate.class));
        assertThat(invocation1.getTestInstance().get(), equalTo(delegate));
        assertThat(invocation1.getTestMethod().get(), equalTo(method));
    }

}
