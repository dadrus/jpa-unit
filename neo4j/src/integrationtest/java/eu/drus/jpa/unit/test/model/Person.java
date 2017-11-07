package eu.drus.jpa.unit.test.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @OneToMany(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH
    })
    private Set<Person> friend = new HashSet<>();

    @OneToMany(cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH
    })
    private Set<Technology> expertIn = new HashSet<>();

    protected Person() {
        // for JPA
    }

    public Person(final String name, final String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public boolean addToFriends(final Person p) {
        return friend.add(p);
    }

    public boolean removeFromFriends(final Person p) {
        return friend.remove(p);
    }

    public Set<Person> getFriends() {
        return Collections.unmodifiableSet(friend);
    }

    public boolean addExpertiseIn(final Technology t) {
        return expertIn.add(t);
    }

    public boolean removeFromExpertiseIn(final Technology t) {
        return expertIn.remove(t);
    }

    public Set<Technology> getExpertiseIn() {
        return Collections.unmodifiableSet(expertIn);
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", id);
        builder.append("name", name);
        builder.append("surname", surname);
        return builder.build();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(name);
        builder.append(surname);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Person) {
            final Person other = (Person) obj;
            final EqualsBuilder builder = new EqualsBuilder();
            builder.append(name, other.name);
            builder.append(surname, other.surname);
            return builder.isEquals();
        }

        return false;
    }
}
