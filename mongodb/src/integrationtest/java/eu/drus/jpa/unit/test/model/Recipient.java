package eu.drus.jpa.unit.test.model;

import javax.persistence.Basic;
import javax.persistence.Embeddable;

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
