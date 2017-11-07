package eu.drus.jpa.unit.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.core.DecoratorRegistrar;
import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("org.mockito.*")
@PrepareForTest({
        DecoratorRegistrar.class, JpaUnitContext.class
})
public class JpaUnitTest {

    @Mock
    private TestClassDecorator firstClassDecorator;

    @Mock
    private TestClassDecorator secondClassDecorator;

    @Mock
    private TestMethodDecorator firstMethodDecorator;

    @Mock
    private TestMethodDecorator secondMethodDecorator;

    @Mock
    private ExtensionContext context;

    @Mock
    private JpaUnitContext jpaUnitContext;

    private final Class<?> testClass = getClass();

    @Before
    public void prepareMocks() throws Exception {
        mockStatic(DecoratorRegistrar.class, JpaUnitContext.class);

        when(DecoratorRegistrar.getClassDecorators()).thenReturn(Arrays.asList(firstClassDecorator, secondClassDecorator));
        when(DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator, secondMethodDecorator));

        when(JpaUnitContext.getInstance(any(Class.class))).thenReturn(jpaUnitContext);

        when(firstClassDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);
        when(secondClassDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);
        when(firstMethodDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);
        when(secondMethodDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);

        when(firstClassDecorator.getPriority()).thenReturn(1);
        when(secondClassDecorator.getPriority()).thenReturn(2);

        when(firstMethodDecorator.getPriority()).thenReturn(1);
        when(secondMethodDecorator.getPriority()).thenReturn(2);

        when(context.getTestClass()).thenReturn(Optional.of(testClass));
        when(context.getTestMethod()).thenReturn(Optional.of(getClass().getMethod("prepareMocks")));
        when(context.getExecutionException()).thenReturn(Optional.empty());
        when(context.getTestInstance()).thenReturn(Optional.of(new Object()));
    }

    @Test
    public void testBeforeAllTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeAll(context);

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);

        final InOrder order = inOrder(firstClassDecorator, secondClassDecorator);
        order.verify(firstClassDecorator).beforeAll(invocationCaptor.capture());
        assertThat(invocationCaptor.getValue().getTestClass(), equalTo(testClass));

        order.verify(secondClassDecorator).beforeAll(invocationCaptor.capture());
        assertThat(invocationCaptor.getValue().getTestClass(), equalTo(testClass));

        verifyZeroInteractions(firstMethodDecorator, secondMethodDecorator);
    }

    @Test
    public void testAfterAllTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterAll(context);

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);

        final InOrder order = inOrder(secondClassDecorator, firstClassDecorator);
        order.verify(secondClassDecorator).afterAll(invocationCaptor.capture());
        assertThat(invocationCaptor.getValue().getTestClass(), equalTo(testClass));

        order.verify(firstClassDecorator).afterAll(invocationCaptor.capture());
        assertThat(invocationCaptor.getValue().getTestClass(), equalTo(testClass));

        verifyZeroInteractions(firstMethodDecorator, secondMethodDecorator);
    }

    @Test
    public void testBeforeEachTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeEach(context);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(firstMethodDecorator).beforeTest(notNull(TestInvocation.class));
        order.verify(secondMethodDecorator).beforeTest(notNull(TestInvocation.class));
        verifyZeroInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testAfteerEachTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterEach(context);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(secondMethodDecorator).afterTest(notNull(TestInvocation.class));
        order.verify(firstMethodDecorator).afterTest(notNull(TestInvocation.class));
        verifyZeroInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testBeforeEachInvocationArguments() throws Exception {
        // GIVEN
        when(DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeEach(context);

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(firstMethodDecorator).beforeTest(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertThat(invocation.getTestMethod().get(), equalTo(getClass().getMethod("prepareMocks")));
        assertThat(invocation.getTestClass(), equalTo(getClass()));
        assertFalse(invocation.getException().isPresent());
        assertThat(invocation.getFeatureResolver().shouldCleanupAfter(), equalTo(Boolean.TRUE));
        assertThat(invocation.getTestInstance(), notNullValue());
    }

    @Test
    public void testAfterEachInvocationArguments() throws Exception {
        // GIVEN
        when(DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterEach(context);

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(firstMethodDecorator).afterTest(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertThat(invocation.getTestMethod().get(), equalTo(getClass().getMethod("prepareMocks")));
        assertThat(invocation.getTestClass(), equalTo(getClass()));
        assertFalse(invocation.getException().isPresent());
        assertThat(invocation.getFeatureResolver().shouldCleanupAfter(), equalTo(Boolean.TRUE));
        assertThat(invocation.getTestInstance(), notNullValue());
    }
}
