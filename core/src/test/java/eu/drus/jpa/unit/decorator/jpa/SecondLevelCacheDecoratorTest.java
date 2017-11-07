package eu.drus.jpa.unit.decorator.jpa;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.persistence.Cache;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.drus.jpa.unit.spi.Constants;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class SecondLevelCacheDecoratorTest {

    @Mock
    private TestInvocation invocation;

    @Mock
    private FeatureResolver resolver;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private EntityManagerFactory emf;

    @Mock
    private Cache cache;

    @Before
    public void setupMocks() throws Exception {
        when(invocation.getContext()).thenReturn(ctx);
        when(invocation.getFeatureResolver()).thenReturn(resolver);
        when(ctx.getData(eq(Constants.KEY_ENTITY_MANAGER_FACTORY))).thenReturn(emf);
        when(emf.getCache()).thenReturn(cache);
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsDisabled() throws Throwable {
        // GIVEN
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.beforeTest(invocation);
        fixture.afterTest(invocation);

        // THEN
        verifyNoMoreInteractions(cache, emf);
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsRunInBeforeTestPhaseIfConfiguredForIt() throws Throwable {
        // GIVEN
        when(resolver.shouldEvictCacheBefore()).thenReturn(Boolean.TRUE);
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.beforeTest(invocation);

        // THEN
        verify(cache).evictAll();
        verify(emf, times(0)).close();
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsNotRunInBeforeTestPhaseIfNotConfiguredForIt() throws Throwable {
        // GIVEN
        when(resolver.shouldEvictCacheBefore()).thenReturn(Boolean.FALSE);
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.beforeTest(invocation);

        // THEN
        verifyNoMoreInteractions(cache, emf);
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsRunInAfterTestPhaseIfConfiguredForIt() throws Throwable {
        // GIVEN
        when(resolver.shouldEvictCacheAfter()).thenReturn(Boolean.TRUE);
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.afterTest(invocation);

        // THEN
        verify(cache).evictAll();
        verify(emf, times(0)).close();
    }

    @Test
    public void testEvictionOfSecondLevelCacheIsNotRunInAfterTestPhaseIfNotConfiguredForIt() throws Throwable {
        // GIVEN
        when(resolver.shouldEvictCacheAfter()).thenReturn(Boolean.FALSE);
        final SecondLevelCacheDecorator fixture = new SecondLevelCacheDecorator();

        // WHEN
        fixture.afterTest(invocation);

        // THEN
        verifyNoMoreInteractions(cache, emf);
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
