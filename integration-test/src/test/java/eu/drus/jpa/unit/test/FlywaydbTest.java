package eu.drus.jpa.unit.test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.drus.jpa.unit.JpaUnitRunner;
import eu.drus.jpa.unit.annotation.Bootstrapping;

@RunWith(JpaUnitRunner.class)
public class FlywaydbTest {

    @PersistenceContext(unitName = "my-verification-unit")
    private EntityManager manager;

    @Bootstrapping
    public void prepareDataBase(final DataSource ds) {
        final Flyway flyway = new Flyway();
        flyway.setDataSource(ds);
        flyway.migrate();
    }

    @Test
    public void test1() {

    }

    @Test
    public void test2() {

    }
}
