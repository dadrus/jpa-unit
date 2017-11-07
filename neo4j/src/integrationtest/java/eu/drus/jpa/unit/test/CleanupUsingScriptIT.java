package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
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
import eu.drus.jpa.unit.api.CleanupUsingScripts;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.test.model.Technology;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Cleanup(phase = CleanupPhase.NONE)
public class CleanupUsingScriptIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
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
    @CleanupUsingScripts(phase = CleanupPhase.AFTER, value = "scripts/delete-all.script")
    public void test2() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getExpertiseIn().size(), equalTo(1));
    }

    @Test
    public void test3() {
        // since the entire DB is erased after the execution of the previous test, the query should
        // return an empty result set
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);

        assertTrue(query.getResultList().isEmpty());

        // just seed the DB with some data
        final Person person = new Person("Max", "Payne");
        final Technology technology = new Technology("All kinds of weapons");
        person.addExpertiseIn(technology);

        // by default this test is executed in a transaction which is committed on test return. Thus
        // this entity becomes available for further tests thanks to disabled cleanup
        manager.persist(person);
    }

    @Test
    public void test4() {
        // since clean up is disabled we can work with the entity persisted by the previous test
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getExpertiseIn().size(), equalTo(1));
    }

    @Test
    @CleanupUsingScripts(phase = CleanupPhase.BEFORE, value = "scripts/delete-all.script")
    public void test5() {
        // since the entire DB is erased before the execution of the given test, the query should
        // return an empty result set
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);

        assertTrue(query.getResultList().isEmpty());
    }
}
