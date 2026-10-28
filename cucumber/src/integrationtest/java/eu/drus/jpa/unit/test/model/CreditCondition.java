package eu.drus.jpa.unit.test.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "CREDIT_CONDITION")
public class CreditCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Version
    @Column(name = "VERSION")
    private Long version;

    @Column(name = "DESCRIPTION", length = 1024)
    @Basic(optional = false)
    private String description;

    protected CreditCondition() {
        // for JPA
    }

    public CreditCondition(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
