package eu.drus.jpa.unit.neo4j.test.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class B {

    @Id
    private Long id;
}
