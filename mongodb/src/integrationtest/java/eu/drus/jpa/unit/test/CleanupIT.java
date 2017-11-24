package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.Customer;
import eu.drus.jpa.unit.test.model.Invoice;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CleanupIT {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @Cleanup(phase = CleanupPhase.NONE)
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
    @Cleanup(phase = CleanupPhase.NONE)
    public void test2() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
    }

    @Test
    @Cleanup(phase = CleanupPhase.BEFORE, strategy = CleanupStrategy.STRICT)
    public void test3() {
        // since the entire DB is erased before this test starts, the query should return an empty
        // result set
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
    public void test4() {

        // this entity is from the initial data set
        final Customer entity = manager.find(Customer.class, 106L);
        assertNotNull(entity);

        // this is created by us
        final Customer customer = new Customer("Max", "Payne");
        customer.addAddress(new Address("Unknown", "111111", "Unknown", "Unknown"));
        customer.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));
        customer.addPaymentAccount(new Account("DE74123456780987654321", "DUSSDEDDXXX"));

        manager.persist(customer);
    }

    @Test
    public void test5() {
        // since the previous test has defined to delete only the data imported by data sets, only
        // the manually created entity should remain.
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
    }

    @Test
    public void test6() {
        // the default behavior is to erase the whole DB after the test has been executed. This way
        // the query should return an empty result set

        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @InitialDataSets("datasets/initial-data.json")
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_TABLES_ONLY)
    public void test7() {
        // this entity is from the initial data set
        final Customer entity = manager.find(Customer.class, 106L);
        assertNotNull(entity);

        // this is created by us (rows in tables used by initial data set)
        final Customer customer = new Customer("Max", "Payne");
        final Address address = new Address("Unknown", "111111", "Unknown", "Unknown");
        customer.addAddress(address);
        customer.addContactDetail(new ContactDetail(ContactType.EMAIL, "max@payne.com"));
        customer.addPaymentAccount(new Account("DE74123456780987654321", "DUSSDEDDXXX"));

        manager.persist(customer);

        final Invoice invoice = new Invoice(customer, address, "some weapons bought");
        manager.persist(invoice);
    }

    @Test
    @Cleanup
    public void test8() {
        // depositor table is empty (and all related tables as well)
        final TypedQuery<Customer> customerQuery = manager.createQuery("SELECT c FROM Customer c", Customer.class);
        assertTrue(customerQuery.getResultList().isEmpty());

        // but invoice not
        final TypedQuery<Invoice> invoiceQuery = manager.createQuery("SELECT i FROM Invoice i", Invoice.class);
        assertFalse(invoiceQuery.getResultList().isEmpty());
    }
}
