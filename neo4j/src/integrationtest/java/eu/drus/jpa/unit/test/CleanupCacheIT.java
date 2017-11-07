package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupCache;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CleanupCacheIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-cache-unit")
    private EntityManager manager;

    @Before
    public void configureEntityManager() {
        // These are defaults but put here for documentation purposes
        manager.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.USE);
        manager.setProperty("javax.persistence.cache.retrieveMode", CacheRetrieveMode.USE);
    }

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    public void test1() {
        final Person entity = manager.find(Person.class, 100L);

        assertNotNull(entity);
        entity.setName("David");
    }

    @Test
    @Cleanup(phase = CleanupPhase.BEFORE)
    public void test2() {
        // Even the DB is explicitly deleted before the actual test start, we can still find our
        // entity thanks to the used second level cache
        final Person entity = manager.find(Person.class, 100L);
        assertNotNull(entity);

        assertThat(entity.getName(), equalTo("David"));
    }

    @Test
    @CleanupCache(phase = CleanupPhase.BEFORE)
    public void test3() {
        // Cleaning the second level cache will make it now impossible to find the entity
        final Person entity = manager.find(Person.class, 100L);
        assertNull(entity);
    }

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    public void test4() {
        final Person entity = manager.find(Person.class, 100L);

        assertNotNull(entity);
    }

    @Test
    @CleanupCache(phase = CleanupPhase.AFTER)
    public void test5() {
        // Even the DB is implicitly deleted after the previous test, we can still find our
        // entity thanks to the used second level cache
        final Person entity = manager.find(Person.class, 100L);
        assertNotNull(entity);

        // Cleaning the second level cache will make it impossible for the next test to find the
        // entity
    }

    @Test
    public void test6() {
        final Person entity = manager.find(Person.class, 100L);
        assertNull(entity);
    }
}
