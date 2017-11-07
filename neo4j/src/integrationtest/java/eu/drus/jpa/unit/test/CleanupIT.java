package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.CookingRecipe;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.test.model.Technology;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CleanupIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @Cleanup(phase = CleanupPhase.NONE)
    public void test1() {
        // just seed the DB with some data
        final Person person = new Person("Max", "Payne");
        final Technology technology = new Technology("All kinds of weapons");
        person.addExpertiseIn(technology);

        // by default this test is executed in a transaction which is committed on test return. Thus
        // this entity becomes available for further tests thanks to disabled cleanup
        manager.persist(person);
    }

    @Test
    @Cleanup(phase = CleanupPhase.NONE)
    public void test2() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getExpertiseIn().size(), equalTo(1));
    }

    @Test
    @Cleanup(phase = CleanupPhase.BEFORE, strategy = CleanupStrategy.STRICT)
    public void test3() {
        // since the entire DB is erased before this test starts, the query should return an empty
        // result set
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_ROWS_ONLY)
    public void test4() {

        // this entity is from the initial data set
        final Person entity = manager.find(Person.class, 100L);
        assertNotNull(entity);
        assertThat(entity.getFriends().size(), equalTo(2));

        // this is created by us
        final Person person = new Person("Max", "Payne");
        final Technology technology = new Technology("All kinds of weapons");
        person.addExpertiseIn(technology);

        manager.persist(person);
    }

    @Test
    public void test5() {
        // since the previous test has defined to delete only the data imported by data sets, only
        // the manually created entity should remain.
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getExpertiseIn().size(), equalTo(1));
    }

    @Test
    public void test6() {
        // the default behavior is to erase the whole DB after the test has been executed. This way
        // the query should return an empty result set

        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);

        assertTrue(query.getResultList().isEmpty());
    }

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    @Cleanup(phase = CleanupPhase.AFTER, strategy = CleanupStrategy.USED_TABLES_ONLY)
    public void test7() {
        // this entity is from the initial data set
        final Person entity = manager.find(Person.class, 100L);
        assertNotNull(entity);
        assertThat(entity.getFriends().size(), equalTo(2));

        // this is created by us (rows in tables used by initial data set)
        final Person person = new Person("Max", "Payne");
        final Technology technology = new Technology("All kinds of weapons");
        person.addExpertiseIn(technology);

        manager.persist(person);

        manager.persist(new CookingRecipe("Muffin", "A tasty one"));
    }

    @Test
    @Cleanup
    public void test8() {
        // depositor table is empty (and all related tables as well)
        final TypedQuery<Person> depositorQuery = manager.createQuery("SELECT p FROM Person p", Person.class);
        assertTrue(depositorQuery.getResultList().isEmpty());

        // but CookingRecipe not
        final TypedQuery<CookingRecipe> conditionQuery = manager.createQuery("SELECT c FROM CookingRecipe c", CookingRecipe.class);
        assertFalse(conditionQuery.getResultList().isEmpty());
    }
}
