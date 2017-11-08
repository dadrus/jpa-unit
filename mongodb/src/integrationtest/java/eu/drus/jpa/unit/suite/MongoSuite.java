package eu.drus.jpa.unit.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.drus.jpa.unit.test.ApplyCustomScripsIT;
import eu.drus.jpa.unit.test.CleanupCacheIT;
import eu.drus.jpa.unit.test.CleanupIT;
import eu.drus.jpa.unit.test.CleanupUsingScriptIT;
import eu.drus.jpa.unit.test.ExpectedDataSetsIT;
import eu.drus.jpa.unit.test.InitialDataSetsIT;
import eu.drus.jpa.unit.test.TransactionIT;
import eu.drus.jpa.unit.test.TransactionJunit5IT;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(Suite.class)
@SuiteClasses({
        ApplyCustomScripsIT.class, CleanupCacheIT.class, CleanupIT.class, CleanupUsingScriptIT.class, ExpectedDataSetsIT.class,
        InitialDataSetsIT.class, TransactionIT.class, TransactionJunit5IT.class
})
public class MongoSuite {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

}
