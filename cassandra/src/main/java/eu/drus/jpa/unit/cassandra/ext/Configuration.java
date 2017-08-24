package eu.drus.jpa.unit.cassandra.ext;

import com.datastax.driver.core.Cluster;

public interface Configuration {

    Cluster openCluster();

    String getKeySpace();
}
