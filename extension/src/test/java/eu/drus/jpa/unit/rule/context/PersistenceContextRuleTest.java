package eu.drus.jpa.unit.rule.context;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.rule.ExecutionContext;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceContextRuleTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    @SuppressWarnings("unused")
    private EntityManagerFactory emf;

    @Test
    public void testApplyPersistenceContextRule() throws Throwable {
        // GIVEN
        final Field field = getClass().getDeclaredField("emf");
        when(ctx.getPersistenceField()).thenReturn(field);
        final PersistenceContextRule rule = new PersistenceContextRule(ctx);

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, notNullValue());
        assertThat(stmt, instanceOf(PersistenceContextStatement.class));
    }

}
