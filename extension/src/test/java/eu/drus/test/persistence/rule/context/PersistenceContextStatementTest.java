package eu.drus.test.persistence.rule.context;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceContextStatementTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    private EntityManagerFactory emf;

    private EntityManager em;

    @SuppressWarnings("unused")
    private Object someField;

    @Test
    public void testEntityManagerFactoryInjection() throws Throwable {
        // GIVEN
        final Field field = getClass().getDeclaredField("emf");
        final PersistenceContextStatement stmt = new PersistenceContextStatement(entityManagerFactory, field, base, this);

        // WHEN
        stmt.evaluate();

        // THEN
        assertThat(emf, equalTo(entityManagerFactory));
        assertThat(em, nullValue());
        verify(base).evaluate();
        verify(entityManagerFactory, times(0)).createEntityManager();
        verify(entityManager, times(0)).close();
    }

    @Test
    public void testEntityManagerInjection() throws Throwable {
        // GIVEN
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        final Field field = getClass().getDeclaredField("em");
        final PersistenceContextStatement stmt = new PersistenceContextStatement(entityManagerFactory, field, base, this);

        // WHEN
        stmt.evaluate();

        // THEN
        assertThat(emf, nullValue());
        assertThat(em, equalTo(entityManager));
        verify(base).evaluate();
        verify(entityManagerFactory).createEntityManager();
        verify(entityManager).close();
    }

    @Test
    public void testUnsupportedFieldInjection() throws Throwable {
        // GIVEN
        final Field field = getClass().getDeclaredField("someField");
        final PersistenceContextStatement stmt = new PersistenceContextStatement(entityManagerFactory, field, base, this);

        // WHEN
        try {
            stmt.evaluate();
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Unexpected field type"));
        }

        // THEN
        assertThat(emf, nullValue());
        assertThat(em, nullValue());
        verify(base, times(0)).evaluate();
        verify(entityManagerFactory, times(0)).createEntityManager();
        verify(entityManager, times(0)).close();
    }
}
