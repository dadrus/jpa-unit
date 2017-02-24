package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRule;
import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.api.Transactional;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.DepositorRepository;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

@RunWith(CdiTestRunner.class)
public class CdiEnabledJpaUnitTest {

    @Rule
    public JpaUnitRule rule = new JpaUnitRule(getClass());

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @PersistenceContext(unitName = "my-test-unit")
    private static EntityManager manager;

    @Inject
    private EntityManager m1;

    @Inject
    private DepositorRepository repo;

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/initial-data.json")
    @Transactional(TransactionMode.DISABLED)
    public void transactionDisabledTest() {
        final Depositor entity = m1.find(Depositor.class, 100L);

        final List<Depositor> allEntities = repo.findAll();

        assertNotNull(entity);
        entity.setName("David");

        assertTrue(allEntities.contains(entity));
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/initial-data.json")
    @Transactional(TransactionMode.ROLLBACK)
    public void transactionRollbackTest() {
        final Depositor entity = m1.find(Depositor.class, 100L);

        assertNotNull(entity);
        entity.setName("Alex");
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @ExpectedDataSets("datasets/expected-data.json")
    @Transactional(TransactionMode.COMMIT)
    public void transactionCommitTest() throws OperationNotSupportedException {
        final Depositor entity = m1.find(Depositor.class, 100L);

        assertNotNull(entity);
        entity.setName("Max");

        final Set<Account> accounts = entity.getAccounts();

        final GiroAccount giroAccount = accounts.stream().filter(a -> a instanceof GiroAccount).map(a -> (GiroAccount) a).findFirst().get();
        final InstantAccessAccount accessAcount = accounts.stream().filter(a -> a instanceof InstantAccessAccount)
                .map(a -> (InstantAccessAccount) a).findFirst().get();

        giroAccount.deposit(100.0f);
        giroAccount.transfer(150.0f, accessAcount);
    }
}
