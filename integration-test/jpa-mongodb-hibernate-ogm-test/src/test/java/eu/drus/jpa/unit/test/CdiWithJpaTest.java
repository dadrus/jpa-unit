package eu.drus.jpa.unit.test;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.suite.MongoSuite;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(CdiTestRunner.class)
public class CdiWithJpaTest extends AbstractCdiWithJpaTest {

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
