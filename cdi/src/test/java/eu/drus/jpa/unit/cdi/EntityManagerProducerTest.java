package eu.drus.jpa.unit.cdi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Producer;
import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityManagerProducerTest {

    @Mock
    private CreationalContext<EntityManager> context;

    @Mock
    private Producer<EntityManager> delegate;

    private List<EntityManager> producedEntityManagers;

    @Before
    public void clearHolder() {
        EntityManagerHolder.INSTANCE.setEntityManager(null);
        producedEntityManagers = new ArrayList<>();

        when(delegate.produce(eq(context))).thenAnswer((invocation) -> {
            final EntityManager em = mock(EntityManager.class);
            producedEntityManagers.add(em);
            return em;
        });
    }

    @Test
    public void testGetInjectionPoints() {
        // GIVEN
        final EntityManagerProducerProxy producer = new EntityManagerProducerProxy(delegate);

        // WHEN
        producer.getInjectionPoints();

        // THEN
        verify(delegate).getInjectionPoints();
        verifyNoMoreInteractions(delegate);
    }

    @Test(expected = ClassCastException.class)
    public void testStandAloneEntityManagerDispose() {
        // GIVEN
        final EntityManagerProducerProxy producer = new EntityManagerProducerProxy(delegate);

        // WHEN
        producer.dispose(mock(EntityManager.class));

        // THEN
        // ClassCastException is thrown. Given object has not been produced by this producer
    }

    @Test
    public void testProduceAndUseEntityManagerWithoutHavingAnInstanceBackedByTheHolder() {
        // GIVEN
        final EntityManagerProducerProxy producer = new EntityManagerProducerProxy(delegate);

        // WHEN
        final EntityManager instance = producer.produce(context);

        // THEN (only a proxy is created)
        assertThat(instance, not(nullValue()));
        verifyNoMoreInteractions(delegate);

        // WHEN (method is invoked on the proxy)
        instance.clear();

        // THEN (the actual instance is created and used)
        verify(delegate).produce(context);
        verify(producedEntityManagers.get(0)).clear();

        // WHEN (further method is invoked on the proxy)
        instance.close();

        // THEN (previously created instance used)
        verifyNoMoreInteractions(delegate);
        verify(producedEntityManagers.get(0)).close();

        // WHEN
        producer.dispose(instance);

        // THEN (delegate.dispose is called for it)
        verify(delegate).dispose(producedEntityManagers.get(0));
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testProduceAndUseEntityManagerHavingAnInstanceBackedByTheHolder() {
        // GIVEN
        final EntityManager em = mock(EntityManager.class);
        EntityManagerHolder.INSTANCE.setEntityManager(em);
        final EntityManagerProducerProxy producer = new EntityManagerProducerProxy(delegate);

        // WHEN
        final EntityManager instance = producer.produce(context);

        // THEN (only a proxy is created)
        assertThat(instance, not(nullValue()));
        verifyNoMoreInteractions(delegate);

        // WHEN (method is invoked on the proxy)
        instance.clear();

        // THEN (the actual instance is retrieved from the holder and used)
        verifyNoMoreInteractions(delegate);
        verify(em).clear();

        // WHEN (further method is invoked on the proxy)
        instance.close();

        // THEN (previously retrieved instance used)
        verifyNoMoreInteractions(delegate);
        verify(em).close();

        // WHEN
        producer.dispose(instance);

        // THEN (nothing happens since the instance is managed by the test environment)
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testProduceEntityManagerMultipleTimesBakedByHolder() {
        // GIVEN
        final EntityManager em = mock(EntityManager.class);
        EntityManagerHolder.INSTANCE.setEntityManager(em);
        final EntityManagerProducerProxy producer = new EntityManagerProducerProxy(delegate);

        // WHEN
        final EntityManager instance1 = producer.produce(context);
        final EntityManager instance2 = producer.produce(context);

        // THEN (only proxies are created)
        assertThat(instance1, not(nullValue()));
        assertThat(instance2, not(nullValue()));
        assertThat(instance2, not(equalTo(instance1)));

        // WHEN (methods are invoked on the proxies)
        instance1.clear();
        instance2.clear();

        // THEN (the actual instance is retrieved from the holder and used)
        verify(em, times(2)).clear();

        // WHEN (further method is invoked on the proxies)
        instance1.close();
        instance2.close();

        // THEN (previously retrieved instance used)
        verify(em, times(2)).close();

        // WHEN
        producer.dispose(instance1);
        producer.dispose(instance2);

        // THEN (nothing happens since the instance is managed by the test environment)
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void testProduceEntityManagerMultipleTimesNotBakedByHolder() {
        // GIVEN
        final EntityManagerProducerProxy producer = new EntityManagerProducerProxy(delegate);

        // WHEN
        final EntityManager instance1 = producer.produce(context);
        final EntityManager instance2 = producer.produce(context);

        // THEN (only a proxy is created)
        assertThat(instance1, not(nullValue()));
        assertThat(instance2, not(nullValue()));
        assertThat(instance2, not(equalTo(instance1)));

        // WHEN (method is invoked on the proxy)
        instance1.clear();
        instance2.clear();

        // THEN (two actual instances are created and used)
        verify(producedEntityManagers.get(0)).clear();
        verify(producedEntityManagers.get(1)).clear();

        // WHEN (further methods are invoked on the proxies)
        instance1.close();
        instance2.close();

        // THEN (previously created instances are used)
        verify(producedEntityManagers.get(0)).close();
        verify(producedEntityManagers.get(1)).close();

        // WHEN
        producer.dispose(instance1);
        producer.dispose(instance2);

        // THEN (delegate.dispose is called for each actual instance)
        verify(delegate).dispose(producedEntityManagers.get(0));
        verify(delegate).dispose(producedEntityManagers.get(1));
    }
}
