package eu.drus.test.persistence.rule.transaction;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.EntityManagerFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.annotation.TransactionMode;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalRuleTest {

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private FeatureResolverFactory featureResolverFactory;

    @Mock
    private FeatureResolver resolver;

    @Test
    public void testApplyTransactionalRule() throws Throwable {
        // GIVEN
        when(featureResolverFactory.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
        when(resolver.getTransactionMode()).thenReturn(TransactionMode.DISABLED);
        final Field field = getClass().getDeclaredField("emf");
        final TransactionalRule rule = new TransactionalRule(featureResolverFactory, field);

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, notNullValue());
        assertThat(stmt, instanceOf(TransactionalStatement.class));
    }
}
