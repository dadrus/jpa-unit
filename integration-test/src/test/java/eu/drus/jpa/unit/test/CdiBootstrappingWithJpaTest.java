package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class CdiBootstrappingWithJpaTest {

    @Inject
    private EntityManager em;

    @Test
    public void testEntityManagerIsInjected() {
        assertNotNull(em);
        em.clear();
    }
}
