package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.CleanupUsingScripts;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.Customer;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Cleanup(phase = CleanupPhase.NONE)
public class CleanupUsingScriptIT {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    public void test1() {
        // just seed the DB with some data
        final Customer customer = new Customer("Max", "Payne");
        customer.addAddress(new Address("Unknown", "111111", "Unknown", "Unknown"));
        customer.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));
        customer.addContactDetail(new ContactDetail(ContactType.MOBILE, "+1 11 11111111"));
        customer.addPaymentAccount(new Account("DE74123456780987654321", "DUSSDEDDXXX"));

        // by default this test is executed in a transaction which is committed on test return. Thus
        // this entity becomes available for further tests thanks to disabled cleanup
        manager.persist(customer);
    }

    @Test
    @CleanupUsingScripts(phase = CleanupPhase.AFTER, value = "scripts/delete-all.script")
    public void test2() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
    }

    @Test
    public void test3() {
        // since the entire DB is erased after the execution of the previous test, the query should
        // return an empty result set
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);

        assertTrue(query.getResultList().isEmpty());

        // just seed the DB with some data
        final Customer customer = new Customer("Max", "Payne");
        customer.addAddress(new Address("Unknown", "111111", "Unknown", "Unknown"));
        customer.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));
        customer.addContactDetail(new ContactDetail(ContactType.MOBILE, "+1 11 11111111"));
        customer.addPaymentAccount(new Account("DE74123456780987654321", "DUSSDEDDXXX"));

        // by default this test is executed in a transaction which is committed on test return. Thus
        // this entity becomes available for further tests thanks to disabled cleanup
        manager.persist(customer);
    }

    @Test
    public void test4() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
    }

    @Test
    @CleanupUsingScripts(phase = CleanupPhase.BEFORE, value = "scripts/delete-all.script")
    public void test5() {
        // since the entire DB is erased before the execution of the given test, the query should
        // return an empty result set
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);

        assertTrue(query.getResultList().isEmpty());
    }
}
