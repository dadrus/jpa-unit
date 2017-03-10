package eu.drus.jpa.unit.decorator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BootstrappingDecoratorTest {

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final BootstrappingDecorator fixture = new BootstrappingDecorator();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(1));
    }
}
