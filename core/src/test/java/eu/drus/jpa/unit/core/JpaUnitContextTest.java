package eu.drus.jpa.unit.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;
import javax.persistence.PersistenceUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import eu.drus.jpa.unit.api.JpaUnitException;

public class JpaUnitContextTest {

    private static final String SINGLE_UNIT_NAME = "test-unit-1";
    private static final String NO_UNIT_NAME = "test-unit-3";
    private static final String MULTI_UNIT_NAME = "test-unit-4";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Captor
    private ArgumentCaptor<Map<String, Object>> paramsCaptor;

    @Test
    public void testExceptionIsThrownIfNeitherEntityManagerFactoryNorEntityManagerFieldIsPresent() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithoutPersistenceFields.class);

        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testExceptionIsThrownIfBothEntityManagerFactoryAndEntityManagerFieldArePresent() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithEntitiyManagerAndEntityManagerFactory.class);

        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testExceptionIsThrownIfMultipleEntityManagerFactoryFieldsArePresent() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithTwoEntityManagerFactoryFields.class);

        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testExceptionIsThrownIfMultipleEntityManagerFieldsArePresent() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithTwoEntitiyManagerFields.class);

        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testExceptionIsThrownIfFieldAnnotatedWithPersistenceContextIsNotOfTypeEntityManager() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithWrongPersistenceContextType.class);

        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testExceptionIsThrownIfFieldAnnotatedWithPersistenceUnitIsNotOfTypeEntityManagerFactory() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(IllegalArgumentException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithWrongPersistenceUnitType.class);

        // THEN
        // IllegalArgumentException is thrown
    }

    @Test
    public void testPersistenceUnitNameDefinedByPersistenceUnitAnnotationIsUsed() throws Exception {
        // GIVEN

        // WHEN
        final JpaUnitContext ctx = JpaUnitContext.getInstance(ClassWithPersistenceUnitType.class);

        // THEN
        assertThat(ctx.getDescriptor().getUnitName(), equalTo(SINGLE_UNIT_NAME));
        assertThat(ctx.getPersistenceField(), equalTo(ClassWithPersistenceUnitType.class.getDeclaredField("emf")));
    }

    @Test
    public void testPersistenceUnitNameDefinedByPersistenceContextAnnotationIsUsed() throws Exception {
        // GIVEN

        // WHEN
        final JpaUnitContext ctx = JpaUnitContext.getInstance(ClassWithPersistenceContextType.class);

        // THEN
        assertThat(ctx.getDescriptor().getUnitName(), equalTo(SINGLE_UNIT_NAME));
        assertThat(ctx.getPersistenceField(), equalTo(ClassWithPersistenceContextType.class.getDeclaredField("em")));
    }

    @Test
    public void testExceptionIsThrownIfNoDescriptorsForSpecifiedUnitNameCanBeFound() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(JpaUnitException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithPersistenceContextWithoutDescriptors.class);

        // THEN
        // JpaUnitException is thrown
    }

    @Test
    public void testExceptionIsThrownIfMultipleDescriptorsForSpecifiedUnitNameAreFound() throws Exception {
        // GIVEN

        // EXPECT
        expectedException.expect(JpaUnitException.class);

        // WHEN
        JpaUnitContext.getInstance(ClassWithPersistenceContextWithMultipleDescriptors.class);

        // THEN
        // JpaUnitException is thrown
    }

    @Test
    public void testCacheFunctionality() throws Exception {
        // GIVEN

        // WHEN
        final JpaUnitContext ctx = JpaUnitContext.getInstance(ClassWithPersistenceContextType.class);
        ctx.storeData("key", "foo");

        // THEN
        assertThat(ctx.getData("key"), equalTo("foo"));
    }

    private static class ClassWithoutPersistenceFields {}

    private static class ClassWithEntitiyManagerAndEntityManagerFactory {
        @PersistenceContext
        private EntityManager em;
        @PersistenceUnit
        private EntityManagerFactory emf;
    }

    private static class ClassWithTwoEntitiyManagerFields {
        @PersistenceContext
        private EntityManager em1;
        @PersistenceContext
        private EntityManager em2;
    }

    private static class ClassWithTwoEntityManagerFactoryFields {
        @PersistenceUnit
        private EntityManagerFactory emf1;
        @PersistenceUnit
        private EntityManagerFactory emf2;
    }

    private static class ClassWithWrongPersistenceUnitType {
        @PersistenceUnit
        private EntityManager em;
    }

    private static class ClassWithWrongPersistenceContextType {
        @PersistenceContext
        private EntityManagerFactory emf;
    }

    private static class ClassWithPersistenceContextType {
        @PersistenceContext(unitName = SINGLE_UNIT_NAME, properties = {
                @PersistenceProperty(name = "name1", value = "value1")
        })
        private EntityManager em;
    }

    private static class ClassWithPersistenceUnitType {
        @PersistenceUnit(unitName = SINGLE_UNIT_NAME)
        private EntityManagerFactory emf;
    }

    private static class ClassWithPersistenceContextWithMultipleDescriptors {
        @PersistenceContext(unitName = MULTI_UNIT_NAME)
        private EntityManager em;
    }

    private static class ClassWithPersistenceContextWithoutDescriptors {
        @PersistenceContext(unitName = NO_UNIT_NAME)
        private EntityManager em;
    }
}
