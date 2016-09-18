package eu.drus.test.persistence.dbunit;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

@RunWith(MockitoJUnitRunner.class)
public class DbUnitRuleTest {

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private EntityManager em;

    @Mock
    private FeatureResolverFactory featureResolverFactory;

    @Mock
    private FeatureResolver resolver;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    @Before
    public void setUp() throws Exception {

        when(emf.createEntityManager()).thenReturn(em);
        when(em.getProperties()).thenReturn(Collections.emptyMap());

        when(featureResolverFactory.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
        when(resolver.getSeedData()).thenReturn(Collections.emptyList());
    }

    @Test
    public void testApplyRule() {
        // GIVEN
        final DbUnitRule rule = new DbUnitRule(featureResolverFactory, emf);

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, not(nullValue()));
    }
}
