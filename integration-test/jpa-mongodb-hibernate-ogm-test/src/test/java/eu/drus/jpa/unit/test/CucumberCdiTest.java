package eu.drus.jpa.unit.test;

import org.apache.deltaspike.cdise.api.CdiContainer;
import org.apache.deltaspike.cdise.api.CdiContainerLoader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(Cucumber.class)
@CucumberOptions(strict = false, format = {
        "pretty", "html:target/site/cucumber-pretty", "json:target/cucumber.json"
}, tags = {
        "~@ignore"
}, features = "classpath:bdd-features", glue = "classpath:eu.drus.jpa.unit.test.cucumber.cdi_glue")
public class CucumberCdiTest {

    private static MongodManager manager;
    private static CdiContainer cdiContainer;

    @BeforeClass
    public static void startContainer() {
        cdiContainer = CdiContainerLoader.getCdiContainer();
        cdiContainer.boot();
    }

    @AfterClass
    public static void stopContainer() {
        cdiContainer.shutdown();
    }

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
