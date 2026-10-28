package eu.drus.jpa.unit.cdi;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.persistence.EntityManager;

public class JpaUnitCdiExtension implements Extension {

    public <T> void registerProducer(@Observes final ProcessProducer<T, EntityManager> pb) {
        pb.setProducer(new EntityManagerProducerProxy(pb.getProducer()));
    }
}
