package eu.drus.jpa.unit.test.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Customer {

    // persistence specific attributes

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Version
    private Long version;

    // entity attributes

    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    private String surname;

    @ElementCollection
    private Set<Address> addresses = new HashSet<>();

    @ElementCollection
    private Set<ContactDetail> contactDetails = new HashSet<>();

    @ElementCollection
    private Set<Account> paymentAccounts = new HashSet<>();

    protected Customer() {
        // for JPA
    }

    public Customer(final String name, final String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public boolean addAddress(final Address address) {
        return addresses.add(address);
    }

    public boolean removeAddress(final Address address) {
        return addresses.remove(address);
    }

    public Set<Address> getAddresses() {
        return Collections.unmodifiableSet(addresses);
    }

    public boolean addPaymentAccount(final Account account) {
        return paymentAccounts.add(account);
    }

    public boolean removePaymentAccount(final Account account) {
        return paymentAccounts.remove(account);
    }

    public Set<Account> getPaymentAccounts() {
        return Collections.unmodifiableSet(paymentAccounts);
    }

    public Set<ContactDetail> getContactDetails() {
        return Collections.unmodifiableSet(contactDetails);
    }

    public boolean addContactDetail(final ContactDetail value) {
        return contactDetails.add(value);
    }

    public boolean removeContactDetail(final ContactDetail value) {
        return contactDetails.remove(value);
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", id);
        builder.append("version", version);
        builder.append("name", name);
        builder.append("surname", surname);
        return builder.build();
    }
}
