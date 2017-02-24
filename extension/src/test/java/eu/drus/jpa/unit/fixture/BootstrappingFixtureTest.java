package eu.drus.jpa.unit.fixture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BootstrappingFixtureTest {

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final BootstrappingFixture fixture = new BootstrappingFixture();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(0));
    }
}
