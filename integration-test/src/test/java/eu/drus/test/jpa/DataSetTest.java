package eu.drus.test.jpa;

import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import eu.drus.test.jpa.test_model.Depositor;
import eu.drus.test.persistence.JpaUnitRunner;
import eu.drus.test.persistence.annotation.ExpectedDataSets;
import eu.drus.test.persistence.annotation.InitialDataSets;
import eu.drus.test.persistence.annotation.TransactionMode;
import eu.drus.test.persistence.annotation.Transactional;

@RunWith(JpaUnitRunner.class)
public class DataSetTest {

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    @InitialDataSets("datasets/test-data.json")
    @ExpectedDataSets("datasets/test-data.json")
    @Transactional(TransactionMode.DISABLED)
    public void someReadDataTest1() {
        final Depositor entity = manager.find(Depositor.class, 100L);

        assertNotNull(entity);
        entity.setName("Foo");
    }

    @Test
    @InitialDataSets("datasets/test-data.json")
    @ExpectedDataSets("datasets/test-data.json")
    @Transactional(TransactionMode.ROLLBACK)
    public void someReadDataTest2() {
        final Depositor entity = manager.find(Depositor.class, 100L);

        assertNotNull(entity);
        entity.setName("Foo");
    }

    @Test
    @InitialDataSets("datasets/test-data.json")
    @ExpectedDataSets("datasets/test-data.json")
    @Transactional(TransactionMode.COMMIT)
    public void someReadDataTest3() {
        final Depositor entity = manager.find(Depositor.class, 100L);

        assertNotNull(entity);
        entity.setName("Moo");

        // THEN
        // we've explicitly changed the name and implicitly the version.
        // after the given test function returns, the transaction is committed
        // which will result in a failed expectation
        expectedException.expectMessage("failed in 2 cases");
    }

}
