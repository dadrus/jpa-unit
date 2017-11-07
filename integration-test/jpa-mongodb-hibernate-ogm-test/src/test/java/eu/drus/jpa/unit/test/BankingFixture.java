package eu.drus.jpa.unit.test;

import org.concordion.api.BeforeSuite;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.concordion.JpaUnitConcordionRunner;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(JpaUnitConcordionRunner.class)
public class BankingFixture extends AbstractConcordionFixture {

    @BeforeSuite
    public static void startMongod() {
        MongodManager.startServer();
    }

}
