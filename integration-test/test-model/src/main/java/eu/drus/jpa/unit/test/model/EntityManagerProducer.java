package eu.drus.jpa.unit.test.model;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

public class EntityManagerProducer {

    @PersistenceUnit
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-test-unit");

    @Produces
    public EntityManager create() {
        return emf.createEntityManager();
    }

    public void close(@Disposes final EntityManager em) {
        if (em.isOpen()) {
            em.close();
        }
    }
}
