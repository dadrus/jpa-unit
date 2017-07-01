package eu.drus.jpa.unit.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitRunner.class)
public class TransactionTest extends AbstractTransactionTest {

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
