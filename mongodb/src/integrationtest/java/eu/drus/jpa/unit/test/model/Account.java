package eu.drus.jpa.unit.test.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;

@Embeddable
@MappedSuperclass
public class Account {

    @Column(length = 50, updatable = false)
    @Basic(optional = false)
    private String iban;

    @Column(length = 50, updatable = false)
    @Basic(optional = false)
    private String bic;

    protected Account() {
        // for JPA
    }

    public Account(final String iban, final String bic) {
        this.iban = iban;
        this.bic = bic;
    }

    public String getBic() {
        return bic;
    }

    public String getIban() {
        return iban;
    }

}
