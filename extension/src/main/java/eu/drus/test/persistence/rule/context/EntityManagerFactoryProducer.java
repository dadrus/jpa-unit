package eu.drus.test.persistence.rule.context;

import javax.persistence.EntityManagerFactory;

public interface EntityManagerFactoryProducer {

    EntityManagerFactory createEntityManagerFactory();

    void destroyEntityManagerFactory(final EntityManagerFactory emf);
}
