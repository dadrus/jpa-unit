package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.InitialDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
@Cleanup(phase = CleanupPhase.NONE)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InitialDataSetsIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    private Person someGuy = new Person("Max", "Payne");

    @Test
    @InitialDataSets("datasets/initial-data.xml")
    public void test1() {
        // this test uses the default INSERT DataSeedStrategy
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Bronislav'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getSurname(), equalTo("Orlov"));

        manager.persist(someGuy);
    }

    @Test
    public void test2() {
        // Since cleanup is disabled 6 Person entities do exist.

        final Query query = manager.createNativeQuery("START n=node(*) MATCH (n:Person) RETURN count(n)");
        final Long count = (Long) query.getSingleResult();

        assertThat(count, equalTo(6L));
    }

    @Test
    @InitialDataSets(value = "datasets/initial-data.xml", seedStrategy = DataSeedStrategy.CLEAN_INSERT)
    public void test3() {
        // Since the given data is already present (thanks to the disabled cleanup) usage of INSERT
        // DataSeedStrategy would lead to a constraint violations. But usage of clean insert, will
        // delete ALL data within the tables referenced by the given dataset, insert the
        // corresponding data from the data set and retain other data.
        // Thus: Max Payne entity is deleted

        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p", Person.class);
        final List<Person> persons = query.getResultList();

        assertThat(persons.size(), equalTo(5));

        // insert a new person and update e.g a name of the previously stored person and let's
        // see what REFRESH DataSeedStrategy will do (see next test methods)
        manager.persist(someGuy);

        persons.get(0).setName("Foo");
    }

    @Test
    public void test4() {
        // Just to verify the surname update has been persisted
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Foo'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Foo"));
        assertThat(entity.getSurname(), equalTo("Orlov"));

        // and to remove all the friends from Orlov. All of them will be connected thanks to REFRESH
        // in the next test
        final Set<Person> friends = new HashSet<>(entity.getFriends());
        assertThat(friends.size(), equalTo(2));
        friends.forEach(entity::removeFromFriends);
    }

    @Test
    @InitialDataSets(value = "datasets/initial-data2.xml", seedStrategy = DataSeedStrategy.REFRESH)
    public void test5() {
        // Since the given data is already present (thanks to the disabled cleanup) usage of REFRESH
        // will update data referenced by the given dataset, add new data and retain other data.
        // Thus: Max Payne entity is still there, the name of Foo is changed back to Bronislav and a
        // new entity Jack is added

        TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.surname='Orlov'", Person.class);
        Person entity = query.getSingleResult();
        assertThat(entity.getName(), equalTo("Bronislav")); // not Foo!!!
        assertThat(entity.getFriends().size(), equalTo(2)); // all are here again
        entity.setName("Foo"); // change it to Foo for the next test again

        // remove the links to friends for the next test case
        final Set<Person> friends = new HashSet<>(entity.getFriends());
        friends.forEach(entity::removeFromFriends);

        query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        entity = query.getSingleResult();
        assertThat(entity.getSurname(), equalTo("Payne"));

        query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Jack'", Person.class);
        entity = query.getSingleResult();
        assertThat(entity.getSurname(), equalTo("Sparrow"));

        // Let see what UPDATE will bring if we delete an object referenced in the dataset
        manager.remove(entity);
    }

    @Test
    @Cleanup
    @InitialDataSets(value = "datasets/initial-data2.xml", seedStrategy = DataSeedStrategy.UPDATE)
    public void test6() {
        // Since the given data is already present (thanks to the disabled cleanup) usage of UPDATE
        // will update data referenced by the given dataset and present in the database. All rows
        // not present in the database are ignored.
        // Thus Bronislav is Bronislav again, Max is still there, and Jack is absent

        TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.surname='Orlov'", Person.class);
        Person entity = query.getSingleResult();
        assertThat(entity.getName(), equalTo("Bronislav")); // not Foo!!!
        assertThat(entity.getFriends().size(), equalTo(2)); // all are here again

        query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        entity = query.getSingleResult();
        assertThat(entity.getSurname(), equalTo("Payne"));

        query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Jack'", Person.class);
        assertTrue(query.getResultList().isEmpty());
    }
}
