package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.test.model.DepositorRepository;

/*
 * The solely purpose of this test is to show that even the CDI extension is in the classpath, it
 * does not affect a regular CDI test which does not use JPA Unit. Here the EntityManagerProducer is
 * used to produce and dispose the EntityManager.
 */

@RunWith(CdiTestRunner.class)
public class CdiWithJpaTest {

    @Inject
    private DepositorRepository repository;

    @Test
    public void testEntityManagerIsInjected() {
        assertNotNull(repository);
        assertThat(repository.findAll().isEmpty(), is(Boolean.TRUE));
    }
}
