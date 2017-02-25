package eu.drus.jpa.unit.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.api.Bootstrapping;
import eu.drus.jpa.unit.api.JpaUnitRunner;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

@RunWith(JpaUnitRunner.class)
public class LiquibaseTest {

    @PersistenceContext(unitName = "my-verification-unit")
    private EntityManager manager;

    @Bootstrapping
    public void prepareDataBase(final DataSource ds) throws Exception {
        final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(ds.getConnection()));
        final Liquibase liquibase = new Liquibase("changelog/changelog.xml", new ClassLoaderResourceAccessor(), database);
        liquibase.dropAll();
        liquibase.update((String) null);
    }

    @Test
    public void test1() {

    }

    @Test
    public void test2() {

    }
}
