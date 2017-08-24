package eu.drus.jpa.unit.cassandra.dataset;

import java.util.Optional;

public interface DataSet extends Iterable<Table> {

    Optional<Table> getTable(String name);
}
