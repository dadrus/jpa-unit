package eu.drus.test.persistence.rule.cache;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;
import eu.drus.test.persistence.rule.context.EntityManagerFactoryProducer;

@RunWith(MockitoJUnitRunner.class)
public class SecondLevelCacheRuleTest {

    @Mock
    private FeatureResolverFactory featureResolverFactory;

    @Mock
    private FeatureResolver resolver;

    @Mock
    private EntityManagerFactoryProducer emfProducer;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    @Before
    public void setupMocks() {
        when(featureResolverFactory.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
    }

    @Test
    public void testApplySecondLevelCacheRule() {

        // GIVEN
        final SecondLevelCacheRule rule = new SecondLevelCacheRule(featureResolverFactory, emfProducer);

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, notNullValue());
        assertThat(stmt, instanceOf(SecondLevelCacheStatement.class));
    }
}
