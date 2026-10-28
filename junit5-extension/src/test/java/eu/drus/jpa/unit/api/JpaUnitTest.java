package eu.drus.jpa.unit.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import eu.drus.jpa.unit.core.DecoratorRegistrar;
import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

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

    private AutoCloseable mockHolder;

    private MockedStatic<DecoratorRegistrar> decoratorRegistrarMock;

    private MockedStatic<JpaUnitContext> jpaUnitContextMock;

    @BeforeEach
    public void prepareMocks() throws Exception {
        mockHolder = MockitoAnnotations.openMocks(this);

        decoratorRegistrarMock = mockStatic(DecoratorRegistrar.class);
        jpaUnitContextMock = mockStatic(JpaUnitContext.class);

        decoratorRegistrarMock.when(() -> DecoratorRegistrar.getClassDecorators())
                .thenReturn(Arrays.asList(firstClassDecorator, secondClassDecorator));
        decoratorRegistrarMock.when(() -> DecoratorRegistrar.getMethodDecorators())
                .thenReturn(Arrays.asList(firstMethodDecorator, secondMethodDecorator));

        jpaUnitContextMock.when(() -> JpaUnitContext.getInstance(any(Class.class))).thenReturn(jpaUnitContext);

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

    @AfterEach
    public void releaseMocks() throws Exception {
        jpaUnitContextMock.close();
        decoratorRegistrarMock.close();
        mockHolder.close();
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
        assertEquals(testClass, invocationCaptor.getValue().getTestClass());

        order.verify(secondClassDecorator).beforeAll(invocationCaptor.capture());
        assertEquals(testClass, invocationCaptor.getValue().getTestClass());

        verifyNoInteractions(firstMethodDecorator, secondMethodDecorator);
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
        assertEquals(testClass, invocationCaptor.getValue().getTestClass());

        order.verify(firstClassDecorator).afterAll(invocationCaptor.capture());
        assertEquals(testClass, invocationCaptor.getValue().getTestClass());

        verifyNoInteractions(firstMethodDecorator, secondMethodDecorator);
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
        verifyNoInteractions(firstClassDecorator, secondClassDecorator);
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
        verifyNoInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testBeforeEachInvocationArguments() throws Exception {
        // GIVEN
        decoratorRegistrarMock.when(() -> DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.beforeEach(context);

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(firstMethodDecorator).beforeTest(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertEquals(getClass().getMethod("prepareMocks"), invocation.getTestMethod().get());
        assertEquals(getClass(), invocation.getTestClass());
        assertFalse(invocation.getException().isPresent());
        assertEquals(Boolean.TRUE, invocation.getFeatureResolver().shouldCleanupAfter());
        assertNotNull(invocation.getTestInstance());
    }

    @Test
    public void testAfterEachInvocationArguments() throws Exception {
        // GIVEN
        decoratorRegistrarMock.when(() -> DecoratorRegistrar.getMethodDecorators()).thenReturn(Arrays.asList(firstMethodDecorator));
        final JpaUnit unit = new JpaUnit();

        // WHEN
        unit.afterEach(context);

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(firstMethodDecorator).afterTest(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();
        assertNotNull(invocation.getContext());
        assertEquals(getClass().getMethod("prepareMocks"), invocation.getTestMethod().get());
        assertEquals(getClass(), invocation.getTestClass());
        assertFalse(invocation.getException().isPresent());
        assertEquals(Boolean.TRUE, invocation.getFeatureResolver().shouldCleanupAfter());
        assertNotNull(invocation.getTestInstance());
    }
}
