package eu.drus.jpa.unit.spi;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.core.DecoratorRegistrar;

@RunWith(MockitoJUnitRunner.class)
public class DecoratorExecutorTest {

    @Mock
    private TestClassDecorator firstClassDecorator;

    @Mock
    private TestClassDecorator secondClassDecorator;

    @Mock
    private TestMethodDecorator firstMethodDecorator;

    @Mock
    private TestMethodDecorator secondMethodDecorator;

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext jpaUnitContext;

    private MockedStatic<DecoratorRegistrar> decoratorRegistrarMock;

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Before
    public void prepareMocks() {
        decoratorRegistrarMock = mockStatic(DecoratorRegistrar.class);

        decoratorRegistrarMock.when(DecoratorRegistrar::getClassDecorators).thenReturn(Arrays.asList(firstClassDecorator, secondClassDecorator));
        decoratorRegistrarMock.when(DecoratorRegistrar::getMethodDecorators).thenReturn(Arrays.asList(firstMethodDecorator, secondMethodDecorator));

        when(firstClassDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);
        when(secondClassDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);
        when(firstMethodDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);
        when(secondMethodDecorator.isConfigurationSupported(any(ExecutionContext.class))).thenReturn(Boolean.TRUE);

        when(firstClassDecorator.getPriority()).thenReturn(1);
        when(secondClassDecorator.getPriority()).thenReturn(2);

        when(firstMethodDecorator.getPriority()).thenReturn(1);
        when(secondMethodDecorator.getPriority()).thenReturn(2);

        when(invocation.getContext()).thenReturn(jpaUnitContext);
        lenient().when(invocation.getTestClass()).thenReturn((Class) getClass());
    }

    @After
    public void releaseMocks() {
        decoratorRegistrarMock.close();
    }

    @Test
    public void testProcessBeforeAllTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final DecoratorExecutor unit = new DecoratorExecutor();

        // WHEN
        unit.processBeforeAll(invocation);

        // THEN
        final InOrder order = inOrder(firstClassDecorator, secondClassDecorator);
        order.verify(firstClassDecorator).beforeAll(eq(invocation));
        order.verify(secondClassDecorator).beforeAll(eq(invocation));
        verifyNoInteractions(firstMethodDecorator, secondMethodDecorator);
    }

    @Test
    public void testProcessAfterAllTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final DecoratorExecutor unit = new DecoratorExecutor();

        // WHEN
        unit.processAfterAll(invocation);

        // THEN
        final InOrder order = inOrder(secondClassDecorator, firstClassDecorator);
        order.verify(secondClassDecorator).afterAll(eq(invocation));
        order.verify(firstClassDecorator).afterAll(eq(invocation));
        verifyNoInteractions(firstMethodDecorator, secondMethodDecorator);
    }

    @Test
    public void testProcessBeforeTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final DecoratorExecutor unit = new DecoratorExecutor();

        // WHEN
        unit.processBefore(invocation);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(firstMethodDecorator).beforeTest(eq(invocation));
        order.verify(secondMethodDecorator).beforeTest(eq(invocation));
        verifyNoInteractions(firstClassDecorator, secondClassDecorator);
    }

    @Test
    public void testProcessAfterTestDecoratorExecutionOrder() throws Exception {
        // GIVEN
        final DecoratorExecutor unit = new DecoratorExecutor();

        // WHEN
        unit.processAfter(invocation);

        // THEN
        final InOrder order = inOrder(firstMethodDecorator, secondMethodDecorator);
        order.verify(secondMethodDecorator).afterTest(eq(invocation));
        order.verify(firstMethodDecorator).afterTest(eq(invocation));
        verifyNoInteractions(firstClassDecorator, secondClassDecorator);
    }
}
