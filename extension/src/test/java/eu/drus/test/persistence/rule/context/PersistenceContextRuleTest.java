package eu.drus.test.persistence.rule.context;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import javax.persistence.EntityManagerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceContextRuleTest {

    @Mock
    private EntityManagerFactoryProducer emfProducer;

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
        final PersistenceContextRule rule = new PersistenceContextRule(emfProducer, field);

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, notNullValue());
        assertThat(stmt, instanceOf(PersistenceContextStatement.class));
    }

}
