package eu.drus.jpa.unit.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.CookingRecipe;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.test.model.Technology;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
public class ExpectedDataSetsIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    private void createTestData(final EntityManager manager) {
        final Person orlov = new Person("Alexander", "Orlov");
        final Person nagarkar = new Person("Rajesh", "Nagarkar");
        final Person schroeder = new Person("Anna", "Schroeder");
        final Person herman = new Person("Amanda", "Herman");
        final Person adamo = new Person("Ludovico", "Adamo");
        final Person maricela = new Person("Sibylla", "Maricela");

        final Technology javaEE = new Technology("Java EE");
        final Technology neo4j = new Technology("Neo4j");
        final Technology bootstrap = new Technology("Bootstrap");

        orlov.addToFriends(nagarkar);
        orlov.addToFriends(schroeder);
        schroeder.addToFriends(herman);
        herman.addExpertiseIn(neo4j);
        nagarkar.addToFriends(adamo);
        adamo.addExpertiseIn(javaEE);
        nagarkar.addExpertiseIn(bootstrap);
        orlov.addToFriends(maricela);

        manager.persist(orlov);
    }

    @Test
    @ExpectedDataSets(value = "datasets/no-data.xml")
    public void test1() {
        createTestData(manager);

        expectedException.expect(AssertionError.class);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.xml", excludeColumns = "id")
    public void test2() {
        createTestData(manager);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.xml", excludeColumns = "id")
    public void test3() {
        createTestData(manager);

        // adding a new row to a table which is referenced by the expected data set but not included
        // in it will lead to a comparison error. Thus a AssertionError exception is expected
        manager.persist(new Person("Max", "Payne"));

        expectedException.expect(AssertionError.class);
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.xml", excludeColumns = "id")
    public void test4() {
        createTestData(manager);

        // adding a new row to a table which is not referenced by the expected data set will not
        // lead to a comparison error.
        manager.persist(new CookingRecipe("Muffin", "A tasty one"));
    }

    @Test
    @ExpectedDataSets(value = "datasets/expected-data.xml", excludeColumns = "id", strict = true)
    public void test5() {
        createTestData(manager);

        // adding a new row to a table which is not referenced by the expected data set will
        // lead to a comparison error in strict mode.
        manager.persist(new CookingRecipe("Muffin", "A tasty one"));

        expectedException.expect(AssertionError.class);
    }
}
