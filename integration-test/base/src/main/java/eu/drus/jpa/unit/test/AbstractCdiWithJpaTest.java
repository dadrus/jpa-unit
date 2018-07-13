package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import org.junit.Test;

import eu.drus.jpa.unit.test.model.DepositorRepository;

/*
 * The solely purpose of this test is to show that even the CDI extension is in the classpath, it
 * does not affect a regular CDI test which does not use JPA Unit. Here the EntityManagerProducer
 * from the test-model project is used to produce and dispose the EntityManager.
 */

public abstract class AbstractCdiWithJpaTest {

    @Inject
    private DepositorRepository repository;

    @Test
    public void testEntityManagerIsInjected() {
        assertNotNull(repository);
        assertThat(repository.findAll().isEmpty(), is(Boolean.TRUE));
    }
}
