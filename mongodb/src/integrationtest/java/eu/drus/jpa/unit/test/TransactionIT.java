package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.api.Transactional;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Customer;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionIT {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/initial-data.json")
    @Transactional(TransactionMode.DISABLED)
    public void transactionDisabledTest() {
        final Customer entity = manager.find(Customer.class, 106L);

        assertNotNull(entity);
        entity.setName("David");
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/initial-data.json")
    @Transactional(TransactionMode.ROLLBACK)
    public void transactionRollbackTest() {
        final Customer entity = manager.find(Customer.class, 106L);

        assertNotNull(entity);
        entity.setName("Alex");
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/expected-data.json")
    @Transactional(TransactionMode.COMMIT)
    public void transactionCommitTest() {
        final Customer entity = manager.find(Customer.class, 106L);

        assertNotNull(entity);
        entity.setName("Max");

        entity.addPaymentAccount(new Account("DE74876543211234567890", "ESSDEDDXXX"));
    }

}
