package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.test.model.TestObjectRepository;

/**
 * This test verifies that a regular CDI test, which uses JPA functionality without JPA-Unit works
 * as expected (no side effects from JPA-Unit)
 *
 */
@RunWith(CdiTestRunner.class)
public class CdiWithJpaIT {

    @Inject
    private TestObjectRepository repository;

    @Inject
    private EntityManager em;

    @Test
    public void testRepositoryIsInjected() {
        assertNotNull(repository);
    }

    @Test
    public void testEntityManagerIsInjectedAndIsOpen() {
        assertNotNull(em);
        assertTrue(em.isOpen());

        em.getTransaction().begin();
        assertTrue(em.getTransaction().isActive());
        em.getTransaction().commit();
    }

    @Test
    public void testThereIsNoData() {
        assertThat(repository.findAll().isEmpty(), is(Boolean.TRUE));
    }
}
