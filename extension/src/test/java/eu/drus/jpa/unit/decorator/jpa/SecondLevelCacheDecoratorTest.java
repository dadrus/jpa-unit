package eu.drus.jpa.unit.decorator.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import javax.persistence.Cache;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.decorator.jpa.SecondLevelCacheDecorator;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(MockitoJUnitRunner.class)
public class SecondLevelCacheDecoratorTest {

    @Mock
    private TestMethodInvocation invocation;

    @Mock
    private FeatureResolver resolver;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private Cache cache;

    @Before
    public void setupMocks() {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getTarget()).thenReturn(this);
        when(ctx.getData(eq("emf"))).thenReturn(emf);
        when(ctx.createFeatureResolver(any(Method.class), any(Class.class))).thenReturn(resolver);
        when(emf.getCache()).thenReturn(cache);
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsDisabled() throws Throwable {
        // GIVEN
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.apply(invocation);

        // THEN
        verify(invocation).proceed();
        verify(cache, times(0)).evictAll();
        verify(emf, times(0)).close();
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsRunBeforeBaseStatementExecution() throws Throwable {
        // GIVEN
        when(resolver.shouldEvictCacheBefore()).thenReturn(Boolean.TRUE);
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.apply(invocation);

        // THEN
        final InOrder order = inOrder(invocation, cache);
        order.verify(cache).evictAll();
        order.verify(invocation).proceed();
        verify(emf, times(0)).close();
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsRunAfterBaseStatementExecution() throws Throwable {
        // GIVEN
        when(resolver.shouldEvictCacheAfter()).thenReturn(Boolean.TRUE);
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.apply(invocation);

        // THEN
        final InOrder order = inOrder(invocation, cache);
        order.verify(invocation).proceed();
        order.verify(cache).evictAll();
        verify(emf, times(0)).close();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        final int priority = fixture.getPriority();

        // THEN
        assertThat(priority, equalTo(0));
    }
}
