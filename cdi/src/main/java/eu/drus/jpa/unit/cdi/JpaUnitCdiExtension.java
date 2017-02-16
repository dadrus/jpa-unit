package eu.drus.jpa.unit.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.persistence.EntityManager;

public class JpaUnitCdiExtension implements Extension {

    public <T> void registerProducer(@Observes final ProcessProducer<T, EntityManager> pb) {
        pb.setProducer(new EntityManagerProducer(pb.getProducer()));
    }
}
