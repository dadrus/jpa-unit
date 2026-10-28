package eu.drus.jpa.unit.test.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "CONTACT_DETAIL")
public class ContactDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "TYPE", length = 50, updatable = false)
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private ContactType type;

    @Column(name = "VALUE", length = 50)
    @Basic(optional = false)
    private String value;

    protected ContactDetail() {
        // for JPA
    }

    public ContactDetail(final ContactType type, final String value) {
        this.type = type;
        this.value = value;
    }

    public ContactType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(type);
        builder.append(value);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof ContactDetail)) {
            return false;
        }

        final ContactDetail other = (ContactDetail) obj;

        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(type, other.type);
        builder.append(value, other.value);
        return builder.build();
    }
}
