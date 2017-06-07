package eu.drus.jpa.unit.mongodb.integration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.CreditCondition;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CleanupTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @Cleanup(phase = CleanupPhase.NONE)
    public void test1() throws OperationNotSupportedException {
        // just seed the DB with some data
        final Depositor depositor = new Depositor("Max", "Payne");
        depositor.addAddress(new Address("Unknown", "111111", "Unknown", "Unknown"));
        depositor.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));
        final GiroAccount account = new GiroAccount(depositor);
        account.deposit(100000.5f);

        // by default this test is executed in a transaction which is committed on test return. Thus
        // this entity becomes available for further tests thanks to disabled cleanup
        manager.persist(depositor);
    }

    @Test
    @Cleanup(phase = CleanupPhase.NONE)
    public void test2() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='Max'", Depositor.class);
        final Depositor entity = query.getSingleResult();

        assertNotNull(entity);
    }

    @Test
    @Cleanup(phase = CleanupPhase.BEFORE, strategy = CleanupStrategy.STRICT)
    public void test3() {
        // since the entire DB is erased before this test starts, the query should return an empty
        // result set
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='Max'", Depositor.class);

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
    public void test4() throws OperationNotSupportedException {

        // this entity is from the initial data set
        final Depositor entity = manager.find(Depositor.class, 100l);
        assertNotNull(entity);

        // this is created by us
        final Depositor depositor = new Depositor("Max", "Payne");
        depositor.addAddress(new Address("Unknown", "111111", "Unknown", "Unknown"));
        depositor.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));
        final GiroAccount account = new GiroAccount(depositor);
        account.deposit(100000.0f);

        manager.persist(depositor);
    }

    @Test
    public void test5() {
        // since the previous test has defined to delete only the data imported by data sets, only
        // the manually created entity should remain.
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d", Depositor.class);
        final Depositor entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
    }

    @Test
    public void test6() {
        // the default behavior is to erase the whole DB after the test has been executed. This way
        // the query should return an empty result set

        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='Max'", Depositor.class);

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_TABLES_ONLY)
    public void test7() throws OperationNotSupportedException {
        // this entity is from the initial data set
        final Depositor entity = manager.find(Depositor.class, 100l);
        assertNotNull(entity);

        // this is created by us (rows in tables used by initial data set)
        final Depositor depositor = new Depositor("Max", "Payne");
        depositor.addAddress(new Address("Unknown", "111111", "Unknown", "Unknown"));
        depositor.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));

        manager.persist(depositor);

        final CreditCondition condition = new CreditCondition("Some description");
        manager.persist(condition);
    }

    @Test
    @Cleanup
    public void test8() {
        // depositor table is empty (and all related tables as well)
        final TypedQuery<Depositor> depositorQuery = manager.createQuery("SELECT d FROM Depositor d", Depositor.class);
        assertTrue(depositorQuery.getResultList().isEmpty());

        // but credit_condition not
        final TypedQuery<CreditCondition> conditionQuery = manager.createQuery("SELECT c FROM CreditCondition c", CreditCondition.class);
        assertFalse(conditionQuery.getResultList().isEmpty());
    }
}
