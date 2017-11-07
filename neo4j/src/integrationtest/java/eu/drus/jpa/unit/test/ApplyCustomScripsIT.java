package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import eu.drus.jpa.unit.api.ApplyScriptsAfter;
import eu.drus.jpa.unit.api.ApplyScriptsBefore;
import eu.drus.jpa.unit.api.Cleanup;
import eu.drus.jpa.unit.api.CleanupPhase;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.test.model.Technology;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Cleanup(phase = CleanupPhase.NONE)
public class ApplyCustomScripsIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-test-unit")
    private EntityManager manager;

    @Test
    @ApplyScriptsBefore("scripts/create-detective-Max-Payne.script")
    @ApplyScriptsAfter("scripts/add-friend-to-Max-Payne.script")
    public void test1() {
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getSurname(), equalTo("Payne"));

        assertThat(entity.getExpertiseIn(), hasItems(new Technology("All kinds of weapons"), new Technology("Detective work")));
        assertThat(entity.getFriends().isEmpty(), equalTo(Boolean.TRUE));
    }

    @Test
    @Cleanup
    public void test2() {
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getSurname(), equalTo("Payne"));

        assertThat(entity.getExpertiseIn(), hasItems(new Technology("All kinds of weapons"), new Technology("Detective work")));
        assertThat(entity.getFriends(), hasItems(new Person("Alex", "Balder")));
    }

}
