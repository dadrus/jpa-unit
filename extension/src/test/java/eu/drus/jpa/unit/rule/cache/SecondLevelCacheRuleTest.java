package eu.drus.jpa.unit.rule.cache;

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

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.ExecutionContext;

@RunWith(MockitoJUnitRunner.class)
public class SecondLevelCacheRuleTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private FeatureResolver resolver;

    @Mock
    private Statement base;

    @Mock
    private FrameworkMethod method;

    @Before
    public void setupMocks() {
        when(ctx.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
    }

    @Test
    public void testApplySecondLevelCacheRule() {

        // GIVEN
        final SecondLevelCacheRule rule = new SecondLevelCacheRule(ctx);

        // WHEN
        final Statement stmt = rule.apply(base, method, this);

        // THEN
        assertThat(stmt, notNullValue());
        assertThat(stmt, instanceOf(SecondLevelCacheStatement.class));
    }
}
