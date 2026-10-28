package eu.drus.jpa.unit.test.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityA {

    @Id
    private Long id;
}
