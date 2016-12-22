package eu.drus.test.persistence.core.dbunit.ext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.junit.Ignore;
import org.junit.Test;

public class Oracle10ConnectionFactoryTest {
    private static final DbUnitConnectionFactory FACTORY = new Oracle10ConnectionFactory();

    @Test
    public void testDriverClassSupport() {
        assertTrue(FACTORY.supportsDriver("oracle.jdbc.OracleDriver"));
        assertTrue(FACTORY.supportsDriver("oracle.jdbc.driver.OracleDriver"));
    }

    @Test
    @Ignore("oracle jdbc required in classpath")
    public void testCreateConnection() throws DatabaseUnitException {
        // WHEN
        final IDatabaseConnection connection = FACTORY.createConnection(mock(Connection.class));

        // THEN
        assertThat(connection, notNullValue());

        final Object typeFactory = connection.getConfig().getProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY);
        assertThat(typeFactory, notNullValue());
        assertThat(typeFactory.getClass(), not(equalTo(DefaultDataTypeFactory.class)));
    }
}
