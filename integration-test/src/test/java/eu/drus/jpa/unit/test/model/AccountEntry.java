package eu.drus.jpa.unit.test.model;

import java.sql.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "ACCOUNT_ENTRY")
public class AccountEntry {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "DATE", updatable = false)
    @Basic(optional = false)
    private Date date;

    @Column(name = "REFERENCE", updatable = false, length = 50)
    @Basic(optional = false)
    private String reference;

    @Column(name = "DETAILS", updatable = false, length = 50)
    @Basic(optional = false)
    private String details;

    @Column(name = "AMOUNT", updatable = false)
    @Basic(optional = false)
    private double amount;

    @Column(name = "TYPE", length = 50, updatable = false)
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private AccountEntryType type;

    protected AccountEntry() {
        // for JPA
    }

    public AccountEntry(final Date date, final String reference, final String details, final float amount, final AccountEntryType type) {
        this.date = date;
        this.reference = reference;
        this.details = details;
        this.amount = amount;
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }

    public String getReference() {
        return reference;
    }

    public double getAmount() {
        return amount;
    }

    public AccountEntryType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(date);
        builder.append(reference);
        builder.append(details);
        builder.append(amount);
        builder.append(type);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof AccountEntry)) {
            return false;
        }
        final AccountEntry other = (AccountEntry) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(date, other.date);
        builder.append(reference, other.reference);
        builder.append(details, other.details);
        builder.append(amount, other.amount);
        builder.append(type, other.type);
        return builder.build();
    }
}
