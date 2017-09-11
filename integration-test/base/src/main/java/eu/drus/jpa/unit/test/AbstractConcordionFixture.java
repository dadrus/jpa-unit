package eu.drus.jpa.unit.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

public abstract class AbstractConcordionFixture {

    @PersistenceContext(unitName = "my-test-unit", type = PersistenceContextType.EXTENDED)
    protected EntityManager manager;
}
