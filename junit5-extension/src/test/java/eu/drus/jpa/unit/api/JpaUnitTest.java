package eu.drus.jpa.unit.api;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
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
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

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
    private ContainerExtensionContext containerContext;

    @Mock
    private TestExtensionContext testContext;

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

        when(firstClassDecorator.getPriority()).thenReturn(1);
        when(secondClassDecorator.getPriority()).thenReturn(2);

        when(firstMethodDecorator.getPriority()).thenReturn(1);
        when(secondMethodDecorator.getPriority()).thenReturn(2);

        when(containerContext.getTestClass()).thenReturn(Optional.of(testClass));
        when(containerContext.getTestMethod()).thenReturn(Optional.of(getClass().getMethod("prepareMocks")));
        when(testContext.getTestClass()).thenReturn(Optional.of(testClass));
        when(testContext.getTestMethod()).thenReturn(Optional.of(getClass().getMethod("prepareMocks")));
        when(testContext.getTestException()).thenReturn(Optional.empty());
    }

    @Test
    public void testBeforeAllTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeAll(containerContext);

        // THEN
        final InOrder order = inOrder(firstClassDecorator, secondClassDecorator);
        order.verify(firstClassDecorator).beforeAll(notNull(ExecutionContext.class), eq(testClass));
        order.verify(secondClassDecorator).beforeAll(notNull(ExecutionContext.class), eq(testClass));
        verifyZeroInteractions(firstMethodDecorator, secondMethodDecorator);
    }

    @Test
    public void testAfterAllTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterAll(containerContext);

        // THEN
        final InOrder order = inOrder(secondClassDecorator, firstClassDecorator);
        order.verify(secondClassDecorator).afterAll(notNull(ExecutionContext.class), eq(testClass));
        order.verify(firstClassDecorator).afterAll(notNull(ExecutionContext.class), eq(testClass));
        verifyZeroInteractions(firstMethodDecorator, secondMethodDecorator);
    }

    @Test
    public void testPostProcessTestInstanceTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.postProcessTestInstance(this, containerContext);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(firstMethodDecorator).processInstance(eq(this), notNull(TestMethodInvocation.class));
        order.verify(secondMethodDecorator).processInstance(eq(this), notNull(TestMethodInvocation.class));
        verifyZeroInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testBeforeEachTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeEach(testContext);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(firstMethodDecorator).beforeTest(notNull(TestMethodInvocation.class));
        order.verify(secondMethodDecorator).beforeTest(notNull(TestMethodInvocation.class));
        verifyZeroInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testAfteerEachTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterEach(testContext);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(secondMethodDecorator).afterTest(notNull(TestMethodInvocation.class));
        order.verify(firstMethodDecorator).afterTest(notNull(TestMethodInvocation.class));
        verifyZeroInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testPostProcessTestInstanceInvocationArguments() throws Exception {
        // GIVEN
        when(DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.postProcessTestInstance(this, containerContext);

        // THEN
        final ArgumentCaptor<TestMethodInvocation> invocationCaptor = ArgumentCaptor.forClass(TestMethodInvocation.class);
        verify(firstMethodDecorator).processInstance(eq(this), invocationCaptor.capture());

        final TestMethodInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertThat(invocation.getMethod(), equalTo(getClass().getMethod("prepareMocks")));
        assertThat(invocation.getTestClass(), equalTo(getClass()));
        assertFalse(invocation.hasErrors());
    }

    @Test
    public void testBeforeEachInvocationArguments() throws Exception {
        // GIVEN
        when(DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeEach(testContext);

        // THEN
        final ArgumentCaptor<TestMethodInvocation> invocationCaptor = ArgumentCaptor.forClass(TestMethodInvocation.class);
        verify(firstMethodDecorator).beforeTest(invocationCaptor.capture());

        final TestMethodInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertThat(invocation.getMethod(), equalTo(getClass().getMethod("prepareMocks")));
        assertThat(invocation.getTestClass(), equalTo(getClass()));
        assertFalse(invocation.hasErrors());
    }

    @Test
    public void testAfterEachInvocationArguments() throws Exception {
        // GIVEN
        when(DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterEach(testContext);

        // THEN
        final ArgumentCaptor<TestMethodInvocation> invocationCaptor = ArgumentCaptor.forClass(TestMethodInvocation.class);
        verify(firstMethodDecorator).afterTest(invocationCaptor.capture());

        final TestMethodInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertThat(invocation.getMethod(), equalTo(getClass().getMethod("prepareMocks")));
        assertThat(invocation.getTestClass(), equalTo(getClass()));
        assertFalse(invocation.hasErrors());
    }
}
