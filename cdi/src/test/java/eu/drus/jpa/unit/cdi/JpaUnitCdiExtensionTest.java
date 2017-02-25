package eu.drus.jpa.unit.cdi;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JpaUnitCdiExtensionTest {

    @Mock
    private ProcessProducer<Object, EntityManager> producer;

    @Captor
    private ArgumentCaptor<Producer<EntityManager>> producerCaptor;

    @Test
    public void testRegisterProducer() {
        // GIVEN
        final JpaUnitCdiExtension extension = new JpaUnitCdiExtension();

        // WHEN
        extension.registerProducer(producer);

        // THEN
        verify(producer).getProducer();
        verify(producer).setProducer(producerCaptor.capture());
        verifyNoMoreInteractions(producer);

        final Producer<EntityManager> captured = producerCaptor.getValue();
        assertThat(captured, not(nullValue()));
        assertThat(captured, instanceOf(EntityManagerProducer.class));
    }
}
