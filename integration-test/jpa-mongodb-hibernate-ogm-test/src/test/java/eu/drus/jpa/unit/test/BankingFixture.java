package eu.drus.jpa.unit.test;

import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeSuite;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.concordion.JpaUnitConcordionRunner;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitConcordionRunner.class)
public class BankingFixture extends AbstractConcordionFixture {

    private static MongodManager manager;

    @BeforeSuite
    public static void startMongod() {
        manager = new MongodManager();
        manager.startMongod(MongodConfiguration.builder().addHost("localhost", 27017).build());
    }

    @AfterSuite
    public static void stopMongod() throws InterruptedException {
        manager.stopMongod();
    }
}
