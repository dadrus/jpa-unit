package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRule;
import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.api.Transactional;
import eu.drus.jpa.unit.test.model.TestObject;
import eu.drus.jpa.unit.test.model.TestObjectRepository;

@RunWith(CdiTestRunner.class)
public class CdiEnabledJpaUnitIT {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @PersistenceContext(unitName = "my-test-unit")
    private static EntityManager manager;

    @Inject
    private TestObjectRepository repo;

    @Inject
    private EntityManager em;

    @Test
    public void testEntityManagerIsInjectedAndIsOpen() {
        assertNotNull(em);
        assertTrue(em.isOpen());

        assertTrue(em.getTransaction().isActive());
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/initial-data.json")
    @Transactional(TransactionMode.DISABLED)
    public void transactionDisabledTest() {
        final TestObject entity = repo.findBy(1L);

        assertNotNull(entity);
        entity.setValue("Test1");
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/initial-data.json")
    @Transactional(TransactionMode.ROLLBACK)
    public void transactionRollbackTest() {
        final TestObject entity = repo.findBy(1L);

        assertNotNull(entity);
        entity.setValue("Test2");
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/expected-data.json")
    @Transactional(TransactionMode.COMMIT)
    public void transactionCommitTest() {
        final TestObject entity = repo.findBy(1L);

        assertNotNull(entity);
        entity.setValue("Test3");
    }
}
