package eu.drus.jpa.unit.api.concordion;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.concordion.api.Resource;
import org.concordion.api.SpecificationLocator;
import org.junit.Test;
import org.junit.runners.model.InitializationError;

import net.sf.cglib.proxy.Factory;

public class JpaUnitConcordionRunnerTest {

    public static class TestFixture {}

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
        assertThat(testObject1, instanceOf(Factory.class));
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
