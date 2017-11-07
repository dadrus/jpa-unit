package eu.drus.jpa.unit.test.model;

import java.util.List;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.QueryParam;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface DepositorRepository extends EntityRepository<Depositor, Long> {

    @Query("SELECT d FROM Depositor d WHERE d.name=:name")
    List<Depositor> findByName(@QueryParam("name") String name);
}
