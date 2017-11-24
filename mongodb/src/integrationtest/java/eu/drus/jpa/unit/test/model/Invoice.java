package eu.drus.jpa.unit.test.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Version
    private Long version;

    @Embedded
    private Recipient recipient;

    @Embedded
    private Address address;

    private String occasion;

    protected Invoice() {
        // for JPA
    }

    public Invoice(final Customer customer, final Address address, final String occasion) {
        recipient = new Recipient(customer);
        this.address = address;
        this.occasion = occasion;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public Address getAddress() {
        return address;
    }

    public String getOccasion() {
        return occasion;
    }
}
