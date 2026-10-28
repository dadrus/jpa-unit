package eu.drus.jpa.unit.test.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Embeddable;

@Embeddable
public class Recipient {

    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    private String surname;

    protected Recipient() {
        // for JPA
    }

    public Recipient(final Customer customer) {
        name = customer.getName();
        surname = customer.getSurname();
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}
