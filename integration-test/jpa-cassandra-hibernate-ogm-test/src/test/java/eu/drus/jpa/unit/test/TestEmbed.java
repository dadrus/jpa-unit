package eu.drus.jpa.unit.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class TestEmbed {

    @BeforeClass
    public static void startCassandra() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @Test
    public void test() {
        final Cluster cluster = EmbeddedCassandraServerHelper.getCluster();
        final Session connect = cluster.connect();
    }

    @AfterClass
    public static void stopCassandra() {
        EmbeddedCassandraServerHelper.stopEmbeddedCassandra();
    }
}
