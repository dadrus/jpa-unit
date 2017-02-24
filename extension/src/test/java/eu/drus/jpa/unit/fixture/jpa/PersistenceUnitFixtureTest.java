package eu.drus.jpa.unit.fixture.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.fixture.jpa.PersistenceUnitFixture;
import eu.drus.jpa.unit.fixture.spi.ExecutionContext;
import eu.drus.jpa.unit.fixture.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceUnitFixtureTest {

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    private EntityManagerFactory emf;

    @SuppressWarnings("unused")
    private Object someField;

    @Before
    public void setupMocks() {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTarget()).thenReturn(this);
        when(ctx.getData(eq("emf"))).thenReturn(entityManagerFactory);
    }

    @Test
    public void testApplyEntityManagerFactoryInjection() throws Throwable {
        // GIVEN
        final Field field = getClass().getDeclaredField("emf");
        when(ctx.getPersistenceField()).thenReturn(field);
        final PersistenceUnitFixture fixture = new PersistenceUnitFixture();

        // WHEN
        fixture.apply(invocation);

        // THEN
        assertThat(emf, equalTo(entityManagerFactory));
        verify(invocation).proceed();
        verify(entityManagerFactory, times(0)).createEntityManager();
        verify(entityManagerFactory, times(0)).close();
    }

    @Test
    public void testApplyForAnUnsupportedField() throws Throwable {
        // GIVEN
        final Field field = getClass().getDeclaredField("someField");
        when(ctx.getPersistenceField()).thenReturn(field);
        final PersistenceUnitFixture fixture = new PersistenceUnitFixture();

        // WHEN
        fixture.apply(invocation);

        // THEN
        assertThat(emf, nullValue());
        verify(invocation).proceed();
        verify(entityManagerFactory, times(0)).createEntityManager();
        verify(entityManagerFactory, times(0)).close();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final PersistenceUnitFixture fixture = new PersistenceUnitFixture();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(1));
    }
}
