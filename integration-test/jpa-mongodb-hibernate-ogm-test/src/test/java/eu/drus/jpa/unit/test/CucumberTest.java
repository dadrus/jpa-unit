package eu.drus.jpa.unit.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import eu.drus.jpa.unit.suite.MongoSuite;
import eu.drus.jpa.unit.test.util.MongodConfiguration;
import eu.drus.jpa.unit.test.util.MongodManager;

@RunWith(Cucumber.class)
@CucumberOptions(strict = false, format = {
        "pretty", "html:target/site/cucumber-pretty", "json:target/cucumber.json"
}, tags = {
        "~@ignore"
}, features = "classpath:bdd-features", glue = "classpath:eu.drus.jpa.unit.test.cucumber.glue")
public class CucumberTest {

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
