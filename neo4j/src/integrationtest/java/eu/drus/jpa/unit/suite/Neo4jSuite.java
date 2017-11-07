package eu.drus.jpa.unit.suite;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import eu.drus.jpa.unit.test.ApplyCustomScripsIT;
import eu.drus.jpa.unit.test.BootsrappingIT;
import eu.drus.jpa.unit.test.CleanupCacheIT;
import eu.drus.jpa.unit.test.CleanupIT;
import eu.drus.jpa.unit.test.CleanupUsingScriptIT;
import eu.drus.jpa.unit.test.ExpectedDataSetsIT;
import eu.drus.jpa.unit.test.InitialDataSetsIT;
import eu.drus.jpa.unit.test.TransactionalIT;
import eu.drus.jpa.unit.test.TransactionalJunit5IT;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(Suite.class)
@SuiteClasses({
        ApplyCustomScripsIT.class, CleanupCacheIT.class, CleanupIT.class, CleanupUsingScriptIT.class, ExpectedDataSetsIT.class,
        InitialDataSetsIT.class, BootsrappingIT.class, TransactionalIT.class, TransactionalJunit5IT.class
})
public class Neo4jSuite {

    @BeforeClass
    public static void startMongod() {
        Neo4jManager.startServer();
    }
}
