package eu.drus.jpa.unit.test.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
@Table(name = "DEPOSITOR")
public class Depositor {

    // persistence specific attributes

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Integer id;

    @Version
    @Column(name = "VERSION")
    private Integer version;

    // entity attributes

    @Column(name = "NAME")
    @Basic(optional = false)
    private String name;

    @Column(name = "SURNAME")
    @Basic(optional = false)
    private String surname;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "DEPOSITOR_ID")
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "DEPOSITOR_ID")
    private Set<ContactDetail> contactDetails = new HashSet<>();

    @OneToMany(mappedBy = "depositor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Account> accounts = new HashSet<>();

    protected Depositor() {
        // for JPA
    }

    public Depositor(final String name, final String surname) {
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

    public Set<Account> getAccounts() {
        return Collections.unmodifiableSet(accounts);
    }

    protected boolean addAccount(final Account account) {
        return accounts.add(account);
    }

    public boolean removeAccount(final Account account) {
        if (accounts.remove(account)) {
            account.setDepositor(null);
            return true;
        }
        return false;
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

    public Set<ContactDetail> getContactDetails() {
        return Collections.unmodifiableSet(contactDetails);
    }

    public boolean addContactDetail(final ContactDetail contactDetail) {
        return contactDetails.add(contactDetail);
    }

    public boolean removeContactDetail(final ContactDetail contactDetail) {
        return contactDetails.remove(contactDetail);
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
