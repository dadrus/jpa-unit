package eu.drus.jpa.unit.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.liquigraph.core.api.Liquigraph;
import org.liquigraph.core.configuration.Configuration;
import org.liquigraph.core.configuration.ConfigurationBuilder;

import eu.drus.jpa.unit.api.Bootstrapping;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import eu.drus.jpa.unit.test.model.Person;
import eu.drus.jpa.unit.test.model.Technology;
import eu.drus.jpa.unit.util.Neo4jManager;

@RunWith(JpaUnitRunner.class)
public class BootsrappingIT {

    @BeforeClass
    public static void startNeo4j() {
        Neo4jManager.startServer();
    }

    @PersistenceContext(unitName = "my-verification-unit")
    private EntityManager manager;

    @Bootstrapping
    public static void prepareDataBase(final DataSource ds) {
        // @formatter:off
        final Configuration configuration = new ConfigurationBuilder()
                .withDataSource(ds)
                .withMasterChangelogLocation("changelog/changelog.xml")
                .withRunMode()
                .build();
        // @formatter:on

        final Liquigraph liquigraph = new Liquigraph();
        liquigraph.runMigrations(configuration);
    }

    @Test
    public void verifyDatabaseContents() {
        final TypedQuery<Person> query = manager.createQuery("SELECT p FROM Person p WHERE p.name='Max'", Person.class);
        final Person entity = query.getSingleResult();

        assertNotNull(entity);
        assertThat(entity.getName(), equalTo("Max"));
        assertThat(entity.getSurname(), equalTo("Payne"));

        final Set<Person> friends = entity.getFriends();
        assertThat(friends.size(), equalTo(1));
        final Person friend = friends.iterator().next();
        assertThat(friend.getName(), equalTo("Alex"));
        assertThat(friend.getSurname(), equalTo("Balder"));

        final Set<Technology> technologies = entity.getExpertiseIn();
        assertThat(technologies.size(), equalTo(2));

        assertThat(technologies, hasItems(new Technology("Weapons of all kinds"), new Technology("Detective work")));
    }
}
