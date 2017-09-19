package eu.drus.jpa.unit.suite;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.drus.jpa.unit.test.ApplyCustomScripsTest;
import eu.drus.jpa.unit.test.BankingFixture;
import eu.drus.jpa.unit.test.CdiEnabledJpaUnitTest;
import eu.drus.jpa.unit.test.CdiWithJpaTest;
import eu.drus.jpa.unit.test.CleanupCacheTest;
import eu.drus.jpa.unit.test.CleanupTest;
import eu.drus.jpa.unit.test.CleanupUsingScriptTest;
import eu.drus.jpa.unit.test.CucumberCdiTest;
import eu.drus.jpa.unit.test.CucumberTest;
import eu.drus.jpa.unit.test.ExpectedDataSetsTest;
import eu.drus.jpa.unit.test.InitialDataSetsTest;
import eu.drus.jpa.unit.test.TransactionJunit5Test;
import eu.drus.jpa.unit.test.TransactionTest;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(Suite.class)
@SuiteClasses({
        ApplyCustomScripsTest.class, BankingFixture.class, CdiEnabledJpaUnitTest.class, CdiWithJpaTest.class, CleanupCacheTest.class,
        CleanupTest.class, CleanupUsingScriptTest.class, CucumberCdiTest.class, CucumberTest.class, ExpectedDataSetsTest.class,
        InitialDataSetsTest.class, TransactionTest.class, TransactionJunit5Test.class
})
public class MongoSuite {

    private static boolean active = false;

    @BeforeClass
    public static void startMongod() {
        active = true;
        MongodManager.start(MongodConfiguration.builder().addHost("localhost", 27017).build());
    }

    @AfterClass
    public static void stopMongod() throws InterruptedException {
        MongodManager.stop();
    }

    public static boolean isActive() {
        return active;
    }
}
