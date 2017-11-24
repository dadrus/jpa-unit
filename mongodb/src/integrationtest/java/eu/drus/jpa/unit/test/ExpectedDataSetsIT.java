package eu.drus.jpa.unit.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.Customer;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExpectedDataSetsIT {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @PersistenceContext(unitName = "my-test-unit")
    protected EntityManager manager;

    protected Customer customer;

    @Before
    public void createTestData() {
        customer = new Customer("Max", "Doe");
        customer.addAddress(new Address("SomeStreet 1", "12345", "SomeCity", "SomeCountry"));
        customer.addContactDetail(new ContactDetail(ContactType.EMAIL, "john.doe@acme.com"));
        customer.addContactDetail(new ContactDetail(ContactType.TELEPHONE, "+1 22 22222222"));
        customer.addPaymentAccount(new Account("DE74876543211234567890", "ESSDEDDXXX"));
    }

    @Test
    @ExpectedDataSets(value = "datasets/no-data.json", excludeColumns = {
            "_id", "version"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test1() {
        manager.persist(customer);

        expectedException.expect(AssertionError.class);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "_id", "version"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test2() {
        manager.persist(customer);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "_id", "version"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test3() {
        manager.persist(customer);

        // adding a new row to a table which is referenced by the expected data set but not included
        // in it will lead to a comparison error. Thus a AssertionError exception is expected
        manager.persist(new Customer("Max", "Payne"));

        expectedException.expect(AssertionError.class);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "_id", "version"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test4() {
        // adding a new row to a table which is not referenced by the expected data set will not
        // lead to a comparison error.
        customer.addAddress(new Address("SomeStreet 1", "12345", "SomeCity", "SomeCountry"));

        manager.persist(customer);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "_id", "version"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    }, strict = true)
    public void test5() {
        // adding a new row to a table which is not referenced by the expected data set will
        // lead to a comparison error in strict mode.
        customer.addAddress(new Address("SomeStreet 1", "12345", "SomeCity", "SomeCountry"));

        manager.persist(customer);

        expectedException.expect(AssertionError.class);
    }
}
