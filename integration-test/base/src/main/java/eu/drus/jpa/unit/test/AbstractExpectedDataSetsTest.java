package eu.drus.jpa.unit.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.GiroAccount;
import eu.drus.jpa.unit.test.model.InstantAccessAccount;
import eu.drus.jpa.unit.test.model.OperationNotSupportedException;

public abstract class AbstractExpectedDataSetsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @PersistenceContext(unitName = "my-test-unit")
    protected EntityManager manager;

    protected Depositor depositor;

    @Before
    public void createTestData() throws OperationNotSupportedException {
        depositor = new Depositor("Max", "Doe");
        depositor.addContactDetail(new ContactDetail(ContactType.EMAIL, "john.doe@acme.com"));
        depositor.addContactDetail(new ContactDetail(ContactType.TELEPHONE, "+1 22 2222 2222"));
        depositor.addContactDetail(new ContactDetail(ContactType.MOBILE, "+1 11 1111 1111"));
        final InstantAccessAccount instantAccessAccount = new InstantAccessAccount(depositor);
        final GiroAccount giroAccount = new GiroAccount(depositor);
        giroAccount.setCreditLimit(1000.0f);
        giroAccount.deposit(100.0f);
        giroAccount.transfer(150.0f, instantAccessAccount);
    }

    @Test
    @ExpectedDataSets(value = "datasets/no-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test1() {
        manager.persist(depositor);

        expectedException.expect(AssertionError.class);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test2() {
        manager.persist(depositor);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test3() throws OperationNotSupportedException {
        manager.persist(depositor);

        // adding a new row to a table which is referenced by the expected data set but not included
        // in it will lead to a comparison error. Thus a AssertionError exception is expected
        manager.persist(new Depositor("Max", "Payne"));

        expectedException.expect(AssertionError.class);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    })
    public void test4() throws OperationNotSupportedException {
        // adding a new row to a table which is not referenced by the expected data set will not
        // lead to a comparison error.
        depositor.addAddress(new Address("SomeStreet 1", "12345", "SomeCity", "SomeCountry"));

        manager.persist(depositor);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.json", excludeColumns = {
            "ID", "DEPOSITOR_ID", "ACCOUNT_ID", "VERSION"
    }, orderBy = {
            "CONTACT_DETAIL.TYPE", "ACCOUNT_ENTRY.TYPE"
    }, strict = true)
    public void test5() throws OperationNotSupportedException {
        // adding a new row to a table which is not referenced by the expected data set will
        // lead to a comparison error in strict mode.
        depositor.addAddress(new Address("SomeStreet 1", "12345", "SomeCity", "SomeCountry"));

        manager.persist(depositor);

        expectedException.expect(AssertionError.class);
    }
}
