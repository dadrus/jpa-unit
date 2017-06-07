package eu.drus.jpa.unit.test.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

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
