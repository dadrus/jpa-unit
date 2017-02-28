package eu.drus.jpa.unit.decorator.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import eu.drus.jpa.unit.decorator.jpa.EntityManagerFactoryDecorator;

public class EntityManagerFactoryDecoratorTest {
    @Test
    public void testRequiredPriority() {
        // GIVEN
        final EntityManagerFactoryDecorator fixture = new EntityManagerFactoryDecorator();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(1));
    }
}
