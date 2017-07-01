package eu.drus.jpa.unit.test;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(CdiTestRunner.class)
public class CdiWithJpaTest extends AbstractCdiWithJpaTest {

    private static MongodManager manager;

    @BeforeClass
    public static void startMongod() {
        manager = new MongodManager();
        manager.startMongod(MongodConfiguration.builder().addHost("localhost", 27017).build());
    }

    @AfterClass
    public static void stopMongod() throws InterruptedException {
        manager.stopMongod();
    }
}
