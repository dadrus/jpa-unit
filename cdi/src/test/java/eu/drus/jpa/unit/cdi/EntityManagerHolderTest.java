package eu.drus.jpa.unit.cdi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CyclicBarrier;

import javax.persistence.EntityManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityManagerHolderTest {

    @Mock
    private EntityManager em;

    @Test
    public void testSetAndRetrieveEntityManagerWithinSameThread() {
        // GIVEN

        // WHEN
        EntityManagerHolder.INSTANCE.setEntityManager(em);

        // THEN
        final EntityManager entityManager = EntityManagerHolder.INSTANCE.getEntityManager();
        assertThat(entityManager, not(nullValue()));
        assertThat(entityManager, equalTo(em));
    }

    @Test
    public void testSetAndRetrieveEntityManagerInDifferentThreads() throws Exception {
        // GIVEN
        final CyclicBarrier barrier = new CyclicBarrier(2);

        // WHEN
        EntityManagerHolder.INSTANCE.setEntityManager(em);

        // THEN
        final Thread thread = new Thread(() -> {
            final EntityManager entityManager = EntityManagerHolder.INSTANCE.getEntityManager();
            assertThat(entityManager, is(nullValue()));
            try {
                barrier.await();
            } catch (final Exception e) {
                // ignore
            }
        });

        thread.start();
        barrier.await();
    }
}
