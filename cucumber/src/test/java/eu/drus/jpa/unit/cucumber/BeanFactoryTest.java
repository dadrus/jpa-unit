package eu.drus.jpa.unit.cucumber;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import eu.drus.jpa.unit.api.JpaUnitException;

public class BeanFactoryTest {

    @Test
    public void testBeanCreationWithDefaultConstructorWithoutUsingCDI() {
        // GIVEN

        // WHEN
        final ClassA bean = BeanFactory.createBean(ClassA.class);

        // THEN
        assertThat(bean, notNullValue());
    }

    @Test(expected = JpaUnitException.class)
    public void testBeanCreationWithoutDefaultConstructorWithoutUsingCDI() {
        // GIVEN

        // WHEN
        BeanFactory.createBean(ClassB.class);

        // THEN
        // exception is thrown
    }

    @Test(expected = JpaUnitException.class)
    public void testBeanCreationWithErrorDuringConstructionWithoutUsingCDI() {
        // GIVEN

        // WHEN
        BeanFactory.createBean(ClassC.class);

        // THEN
        // exception is thrown
    }

    public static class ClassA {}

    public static class ClassB {
        private ClassB() {}
    }

    public static class ClassC {
        public ClassC() {
            throw new RuntimeException("");
        }
    }
}
