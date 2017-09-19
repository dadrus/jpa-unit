package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.ApplyScriptsAfter;
import eu.drus.jpa.unit.api.ApplyScriptsBefore;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Account;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.Depositor;
import eu.drus.jpa.unit.test.model.GiroAccount;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Cleanup(phase = CleanupPhase.NONE)
public abstract class AbstractApplyCustomScripsTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @ApplyScriptsBefore("scripts/create-depositor-Max-Payne.script")
    @ApplyScriptsAfter("scripts/update-balance-of-Max-Payne.script")
    public void test1() {
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='Max'", Depositor.class);
        final Depositor entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getSurname(), equalTo("Payne"));

        final Set<ContactDetail> contactDetails = entity.getContactDetails();
        assertThat(contactDetails.size(), equalTo(1));
        final ContactDetail contactDetail = contactDetails.iterator().next();
        assertThat(contactDetail.getType(), equalTo(ContactType.EMAIL));
        assertThat(contactDetail.getValue(), equalTo("max@payne.com"));

        final Set<Address> addresses = entity.getAddresses();
        assertThat(addresses.size(), equalTo(1));
        final Address address = addresses.iterator().next();
        assertThat(address.getCountry(), equalTo("Unknown"));
        assertThat(address.getZipCode(), equalTo("111111"));
        assertThat(address.getCity(), equalTo("Unknown"));
        assertThat(address.getStreet(), equalTo("Unknown"));

        final Set<Account> accounts = entity.getAccounts();
        assertThat(accounts.size(), equalTo(1));
        final Account account = accounts.iterator().next();
        assertThat(account, instanceOf(GiroAccount.class));
        final GiroAccount giroAccount = (GiroAccount) account;
        assertThat(giroAccount.getBalance(), equalTo(100000.0));

        assertThat(giroAccount.getCreditLimit(), equalTo(100000.0));
    }

    @Test
    @Cleanup
    public void test2() {
        final TypedQuery<Depositor> query = manager.createQuery("SELECT d FROM Depositor d WHERE d.name='Max'", Depositor.class);
        final Depositor entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getSurname(), equalTo("Payne"));

        final Set<Account> accounts = entity.getAccounts();
        assertThat(accounts.size(), equalTo(1));
        final Account account = accounts.iterator().next();
        assertThat(account, instanceOf(GiroAccount.class));
        final GiroAccount giroAccount = (GiroAccount) account;
        assertThat(giroAccount.getBalance(), equalTo(95000.0));
        assertThat(giroAccount.getCreditLimit(), equalTo(100000.0));
    }
}
