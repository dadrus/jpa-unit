package eu.drus.jpa.unit.test.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "ADDRESS")
@SequenceGenerator(name = "ADDRESS_SEQ")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ADDRESS_SEQ")
    private Long id;

    @Column(name = "STREET", length = 50, updatable = false)
    @Basic(optional = false)
    private String street;

    @Column(name = "ZIP_CODE", length = 6, updatable = false)
    @Basic(optional = false)
    private String zipCode;

    @Column(name = "CITY", length = 25, updatable = false)
    @Basic(optional = false)
    private String city;

    @Column(name = "COUNTRY", length = 50, updatable = false)
    @Basic(optional = false)
    private String country;

    protected Address() {
        // for JPA
    }

    public Address(final String street, final String zipCode, final String city, final String country) {
        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
        this.country = country;
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
