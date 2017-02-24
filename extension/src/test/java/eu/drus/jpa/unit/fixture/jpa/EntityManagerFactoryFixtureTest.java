package eu.drus.jpa.unit.fixture.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import eu.drus.jpa.unit.fixture.jpa.EntityManagerFactoryFixture;

public class EntityManagerFactoryFixtureTest {
    @Test
    public void testRequiredPriority() {
        // GIVEN
        final EntityManagerFactoryFixture fixture = new EntityManagerFactoryFixture();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(1));
    }
}
