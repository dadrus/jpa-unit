package eu.drus.jpa.unit.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.suite.MongoSuite;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
public class CleanupTest extends AbstractCleanupTest {

    @BeforeClass
    public static void startMongod() {
        if (!MongoSuite.isActive()) {
            MongodManager.start(MongodConfiguration.builder().addHost("localhost", 27017).build());
        }
    }

    @AfterClass
    public static void stopMongod() throws InterruptedException {
        if (!MongoSuite.isActive()) {
            MongodManager.stop();
        }
    }
}
