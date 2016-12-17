package eu.drus.test.jpa.test_model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;

import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Depositor {

    // persistence specific attributes

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DEPOSITOR_SEQ_GEN")
    // @SequenceGenerator(name = "DEPOSITOR_SEQ_GEN", sequenceName = "DEPOSITOR_SEQ")
    private Long id;

    @Version
    private Long version;

    // entity attributes

    private String name;
    private String surname;

    protected Depositor() {
        // for JPA
    }

    public Depositor(final String name, final String surname) {
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

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("id", id);
        builder.append("version", version);
        builder.append("name", name);
        builder.append("surname", surname);
        return builder.build();
    }
}
