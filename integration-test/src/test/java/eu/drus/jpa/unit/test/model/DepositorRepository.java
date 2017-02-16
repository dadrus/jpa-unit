package eu.drus.jpa.unit.test.model;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DepositorRepository extends EntityRepository<Depositor, Long> {}
