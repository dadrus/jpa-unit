package eu.drus.jpa.unit.core;

import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public class DecoratorRegistrarTest {

    @Test
    public void testClassDecoratorsAreLoaded() {
        // GIVEN

        // WHEN
        final List<TestClassDecorator> classDecorators = DecoratorRegistrar.getClassDecorators();

        // THEN
        assertFalse(classDecorators.isEmpty());
    }

    @Test
    public void testMethodDecoratorsAreLoaded() {
        // GIVEN

        // WHEN
        final List<TestMethodDecorator> methodDecorators = DecoratorRegistrar.getMethodDecorators();

        // THEN
        assertFalse(methodDecorators.isEmpty());
    }
}
