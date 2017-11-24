package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Iterator;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.ApplyScriptsAfter;
import eu.drus.jpa.unit.api.ApplyScriptsBefore;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Address;
import eu.drus.jpa.unit.test.model.AddressType;
import eu.drus.jpa.unit.test.model.ContactDetail;
import eu.drus.jpa.unit.test.model.ContactType;
import eu.drus.jpa.unit.test.model.Customer;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Cleanup(phase = CleanupPhase.NONE)
public class ApplyCustomScripsIT {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @ApplyScriptsBefore("scripts/create-customer-Max-Payne.script")
    @ApplyScriptsAfter("scripts/update-addresses-of-Max-Payne.script")
    public void test1() {
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        final Customer entity = query.getSingleResult();

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
        final Address address1 = addresses.iterator().next();
        assertThat(address1.getCountry(), equalTo("Unknown"));
        assertThat(address1.getZipCode(), equalTo("111111"));
        assertThat(address1.getCity(), equalTo("Unknown"));
        assertThat(address1.getStreet(), equalTo("Unknown"));
        assertThat(address1.getType(), equalTo(AddressType.INVOICE_AND_SHIPMENT));

    }

    @Test
    @Cleanup
    public void test2() {
        final TypedQuery<Customer> query = manager.createQuery("SELECT c FROM Customer c WHERE c.name='Max'", Customer.class);
        final Customer entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getSurname(), equalTo("Payne"));

        final Set<Address> addresses = entity.getAddresses();
        assertThat(addresses.size(), equalTo(2));

        final Iterator<Address> it = addresses.iterator();
        it.next();
        final Address address2 = it.next();
        assertThat(address2.getCountry(), equalTo("Unknown 2"));
        assertThat(address2.getZipCode(), equalTo("111111"));
        assertThat(address2.getCity(), equalTo("Unknown 2"));
        assertThat(address2.getStreet(), equalTo("Unknown 2"));
        assertThat(address2.getType(), equalTo(AddressType.INVOICE));
    }
}
