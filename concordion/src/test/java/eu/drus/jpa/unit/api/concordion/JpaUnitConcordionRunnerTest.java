package eu.drus.jpa.unit.api.concordion;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import org.concordion.api.Resource;
import org.concordion.api.SpecificationLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.concordion.EnhancedProxy;
import eu.drus.jpa.unit.core.JpaUnitContext;

@RunWith(MockitoJUnitRunner.class)
public class JpaUnitConcordionRunnerTest {

    public static class TestFixture {}

    @Mock
    private JpaUnitContext context;

    private MockedStatic<JpaUnitContext> jpaUnitContextMock;

    @Before
    public void prepareTest() throws Throwable {
        jpaUnitContextMock = mockStatic(JpaUnitContext.class);
        jpaUnitContextMock.when(() -> JpaUnitContext.getInstance(any(Class.class))).thenReturn(context);
    }

    @After
    public void releaseJpaUnitContext() {
        jpaUnitContextMock.close();
    }

    @Test
    public void testSuccessfulInitializationOfJpaUnitConcordionRunner() throws InitializationError {
        new JpaUnitConcordionRunner(TestFixture.class);
    }

    @Test
    public void testCreateTestCreatesAnEnhancedObject() throws Exception {
        // GIVEN
        final JpaUnitConcordionRunner runner = new JpaUnitConcordionRunner(TestFixture.class);

        // WHEN
        final Object testObject1 = runner.createTest();

        // THEN
        assertThat(testObject1, instanceOf(EnhancedProxy.class));
    }

    @Test
    public void testSpecificationLocatorResolvesSpecificationResourceBasedOnTheOriginalFixtureClassName() throws Exception {
        // GIVEN
        final JpaUnitConcordionRunner runner = new JpaUnitConcordionRunner(TestFixture.class);
        final Object fixtureObject = runner.createTest();
        final SpecificationLocator specificationLocator = runner.getSpecificationLocator();

        // WHEN
        final Resource specification = specificationLocator.locateSpecification(fixtureObject, "md");

        // THEN
        assertThat(specification.getPath(), containsString("Test"));
        assertThat(specification.getPath(), not(containsString("$$")));
    }
}
