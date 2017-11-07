package eu.drus.jpa.unit.concordion;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.TestInvocation;
import net.sf.cglib.proxy.Enhancer;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JpaUnitContext.class)
public class JpaUnitFixtureTest {

    public static class TestClass {}

    private TestClass originalFixture;

    @Mock
    private JpaUnitContext ctx;

    @Mock
    private DecoratorExecutor executor;

    private JpaUnitFixture fixture;

    @Before
    public void setUp() throws Exception {
        mockStatic(JpaUnitContext.class);
        when(JpaUnitContext.getInstance(any(Class.class))).thenReturn(ctx);

        originalFixture = new TestClass();

        fixture = new JpaUnitFixture(executor,
                Enhancer.create(originalFixture.getClass(), new ConcordionInterceptor(executor, originalFixture)));
    }

    @Test
    public void testBeforeSpecificationCallsProcessAfterAllOnExecutorWithOriginalClassAndCorrespondingExecutor() throws Exception {
        // GIVEN

        // WHEN
        fixture.beforeSpecification();

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(executor).processBeforeAll(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();
        assertThat(invocation.getTestClass(), equalTo(TestClass.class));
        assertThat(invocation.getContext(), equalTo(ctx));
        assertThat(invocation.getFeatureResolver(), notNullValue());
    }

    @Test
    public void testBeforeSpecificationThrowsJpaUnitExceptionIfAnyExceptionIsThrown() throws Exception {
        // GIVEN
        final Exception exception = new Exception("error");
        doThrow(exception).when(executor).processBeforeAll(any(TestInvocation.class));

        try {
            // WHEN
            fixture.beforeSpecification();
            fail("JpaUnitException is expected");
        } catch (final JpaUnitException e) {
            // THEN
            assertThat(e.getCause(), equalTo(exception));
        }
    }

    @Test
    public void testAfterSpecificationCallsProcessAfterAllOnExecutorWithOriginalClassAndCorrespondingExecutor() throws Exception {
        // GIVEN

        // WHEN
        fixture.afterSpecification();

        // THEN
        final ArgumentCaptor<TestInvocation> invocationCaptor = ArgumentCaptor.forClass(TestInvocation.class);
        verify(executor).processAfterAll(invocationCaptor.capture());

        final TestInvocation invocation = invocationCaptor.getValue();
        assertThat(invocation.getTestClass(), equalTo(TestClass.class));
        assertThat(invocation.getContext(), equalTo(ctx));
        assertThat(invocation.getFeatureResolver(), notNullValue());
    }

    @Test
    public void testAfterSpecificationThrowsJpaUnitExceptionIfAnyExceptionIsThrown() throws Exception {
        // GIVEN
        final Exception exception = new Exception("error");
        doThrow(exception).when(executor).processAfterAll(any(TestInvocation.class));

        try {
            // WHEN
            fixture.afterSpecification();
            fail("JpaUnitException is expected");
        } catch (final JpaUnitException e) {
            // THEN
            assertThat(e.getCause(), equalTo(exception));
        }
    }

    @Test
    public void testSpecificationDescriptionContainsTheNameOfOriginalFixtureClassAndNotOfTheEnhancedClass() {
        // GIVEN

        // WHEN
        final String specificationDescription = fixture.getSpecificationDescription();

        // THEN
        assertThat(specificationDescription, containsString(TestClass.class.getSimpleName()));
        assertThat(specificationDescription, not(containsString("$")));
    }
}
