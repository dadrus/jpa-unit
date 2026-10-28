package eu.drus.jpa.unit.fixtures;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

public abstract class AbstractConcordionFixture {

    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    protected EntityManager manager;
}
