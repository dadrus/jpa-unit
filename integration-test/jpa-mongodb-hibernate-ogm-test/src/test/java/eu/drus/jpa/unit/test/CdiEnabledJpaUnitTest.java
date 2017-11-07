package eu.drus.jpa.unit.test;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(CdiTestRunner.class)
public class CdiEnabledJpaUnitTest extends AbstractCdiEnabledJpaUnitTest {

    @BeforeClass
    public static void startMongod() {
        MongodManager.startServer();
    }

}
