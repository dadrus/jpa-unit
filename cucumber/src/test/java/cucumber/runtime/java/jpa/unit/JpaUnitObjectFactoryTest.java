package cucumber.runtime.java.jpa.unit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cucumber.api.java.en.Then;
import cucumber.api.java.ru.Если;
import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JpaUnitContext.class)
public class JpaUnitObjectFactoryTest {

    @Mock
    private JpaUnitContext ctx;

    @Mock
    private DecoratorExecutor executor;

    @InjectMocks
    private JpaUnitObjectFactory factory;

    @Before
    public void prepareJpaUnitContext() {
        mockStatic(JpaUnitContext.class);
        when(JpaUnitContext.getInstance(any(Class.class))).thenReturn(ctx);
    }

    @Test
    public void testAddClassReturnsAlwaysTrueAndDoesNotHaveAnyEffects() {
        // GIVEN

        // WHEN
        final boolean result = factory.addClass(getClass());

        // THEN
        assertThat(result, equalTo(Boolean.TRUE));
        verifyZeroInteractions(executor);
    }

    @Test
    public void testStartDoesNotHaveAnyEffects() {
        // GIVEN

        // WHEN
        factory.start();

        // THEN
        verifyZeroInteractions(executor);
    }

    @Test
    public void testStopDoesNotHaveAnyEffectsIfNoInstancesWereCreated() {
        // GIVEN

        // WHEN
        factory.stop();

        // THEN
        verifyZeroInteractions(executor);
    }

    @Test
    public void testGetInstanceForSameTypeAlwaysReturnsSameObject() {
        // GIVEN

        // WHEN
        final ClassA obj1 = factory.getInstance(ClassA.class);
        final ClassA obj2 = factory.getInstance(ClassA.class);

        // THEN
        assertNotNull(obj1);
        assertNotNull(obj2);
        assertThat(obj1, equalTo(obj2));
    }

    @Test
    public void testGetInstanceForSameTypeLeadsToExecutionOfProcessBeforeAllOnlyOnce() throws Exception {
        // GIVEN

        // WHEN
        final ClassA obj1 = factory.getInstance(ClassA.class);
        final ClassA obj2 = factory.getInstance(ClassA.class);

        // THEN
        assertNotNull(obj1);
        assertNotNull(obj2);

        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(executor).processBeforeAll(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();

        assertThat(invocation.getTestClass(), equalTo(ClassA.class));
        assertThat(invocation.getContext(), equalTo(ctx));
        assertThat(invocation.getFeatureResolver(), notNullValue());

        verifyNoMoreInteractions(executor);
    }

    @Test
    public void testGetInstanceForDifferentTypesLeadsToExecutionOfProcessBeforeAllForEachType() throws Exception {
        // GIVEN

        // WHEN
        final ClassA obj1 = factory.getInstance(ClassA.class);
        final ClassB obj2 = factory.getInstance(ClassB.class);

        // THEN
        assertNotNull(obj1);
        assertNotNull(obj2);

        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(executor, times(2)).processBeforeAll(invocationCaptor.capture());

        final List<TestInvocation> invocations = invocationCaptor.getAllValues();

        assertThat(invocations.get(0).getTestClass(), equalTo(ClassA.class));
        assertThat(invocations.get(0).getContext(), equalTo(ctx));
        assertThat(invocations.get(0).getFeatureResolver(), notNullValue());

        assertThat(invocations.get(1).getTestClass(), equalTo(ClassB.class));
        assertThat(invocations.get(1).getContext(), equalTo(ctx));
        assertThat(invocations.get(1).getFeatureResolver(), notNullValue());

        assertThat(invocations.get(0), not(equalTo(invocations.get(1))));

        verifyNoMoreInteractions(executor);
    }

    @Test
    public void testStopLeadsToExecutionOfProcessAfterAllForEachType() throws Exception {
        // GIVEN
        factory.getInstance(ClassA.class);
        factory.getInstance(ClassB.class);

        // WHEN
        factory.stop();

        // THEN
        verify(executor, times(2)).processBeforeAll(any(TestInvocation.class));
        verify(executor, times(2)).processAfterAll(any(TestInvocation.class));
        verifyNoMoreInteractions(executor);
    }

    @Test
    public void testMethodsWithCucumberAnnotationsAreIntercepted() throws Exception {
        // GIVEN
        final ClassB obj = factory.getInstance(ClassB.class);

        // WHEN
        obj.when();

        // THEN
        verify(executor).processBeforeAll(notNull(TestInvocation.class));
        final InOrder inOrder = inOrder(executor);
        inOrder.verify(executor).processBefore(notNull(TestInvocation.class));
        inOrder.verify(executor).processAfter(notNull(TestInvocation.class));

        verifyNoMoreInteractions(executor);
    }

    @Test
    public void testMethodsWithoutCucumberAnnotationsAreNotIntercepted() throws Exception {
        // GIVEN
        final ClassB obj = factory.getInstance(ClassB.class);

        // WHEN
        obj.notACucumberMethod();

        // THEN
        verify(executor).processBeforeAll(notNull(TestInvocation.class));
        verifyNoMoreInteractions(executor);
    }

    public static class ClassA {}

    public static class ClassB {
        @Если("foo")
        public void when() {}

        @Then("moo")
        public void then() {}

        public void notACucumberMethod() {}
    }
}
