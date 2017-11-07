package eu.drus.jpa.unit.test;

import static org.junit.Assert.assertNotNull;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.api.TransactionMode;
import eu.drus.jpa.unit.api.Transactional;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionalIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    // Some persistence provider always requiresan active transaction for dbs supporting
    // transactions E.g. see also https://hibernate.atlassian.net/browse/OGM-1322 for hibernate OGM

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    @ExpectedDataSets("datasets/initial-data.xml")
    @Transactional(TransactionMode.ROLLBACK)
    public void transactionRollbackTest() {
        final Person entity = manager.find(Person.class, 100L);

        assertNotNull(entity);
        entity.setName("Alexander");
    }

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    @ExpectedDataSets("datasets/expected-data.xml")
    @Transactional(TransactionMode.COMMIT)
    public void transactionCommitTest() {
        final Person entity = manager.find(Person.class, 100L);

        assertNotNull(entity);
        entity.setName("Alexander");
        entity.addToFriends(new Person("Sibylla", "Maricela"));
    }
}
