package eu.drus.jpa.unit.test.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Embeddable
public class Address {

    @Column(length = 50, updatable = false)
    @Basic(optional = false)
    private String street;

    @Column(length = 6, updatable = false)
    @Basic(optional = false)
    private String zipCode;

    @Column(length = 25, updatable = false)
    @Basic(optional = false)
    private String city;

    @Column(length = 50, updatable = false)
    @Basic(optional = false)
    private String country;

    @Column(length = 50, updatable = false)
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private AddressType type;

    protected Address() {
        // for JPA
    }

    public Address(final String street, final String zipCode, final String city, final String country) {
        this(street, zipCode, city, country, AddressType.INVOICE_AND_SHIPMENT);
    }

    public Address(final String street, final String zipCode, final String city, final String country, final AddressType type) {
        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
        this.type = type;
    }

    public String getStreet() {
        return street;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public AddressType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(street);
        builder.append(zipCode);
        builder.append(city);
        builder.append(country);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Address)) {
            return false;
        }
        final Address other = (Address) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(street, other.street);
        builder.append(zipCode, other.zipCode);
        builder.append(city, other.city);
        builder.append(country, other.country);
        return builder.build();
    }
}
