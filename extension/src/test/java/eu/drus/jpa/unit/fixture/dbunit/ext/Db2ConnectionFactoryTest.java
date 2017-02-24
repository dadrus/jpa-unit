package eu.drus.jpa.unit.fixture.dbunit.ext;

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
import org.junit.Test;

import eu.drus.jpa.unit.fixture.dbunit.ext.Db2ConnectionFactory;
import eu.drus.jpa.unit.fixture.dbunit.ext.DbUnitConnectionFactory;

public class Db2ConnectionFactoryTest {

    private static final DbUnitConnectionFactory FACTORY = new Db2ConnectionFactory();

    @Test
    public void testDriverClassSupport() {
        assertTrue(FACTORY.supportsDriver("com.ibm.db2.jcc.DB2Driver"));
    }

    @Test
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
