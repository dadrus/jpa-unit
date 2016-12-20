package eu.drus.test.persistence.core.dbunit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2Connection;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import eu.drus.test.persistence.JpaUnitException;

// @Ignore("FIXME")
public class DatabaseConnectionFactoryTest {

    private static final String CONNECTION_URL_PROP_NAME = "javax.persistence.jdbc.url";
    private static final String DRIVER_CLASS_PROP_NAME = "javax.persistence.jdbc.driver";
    private static final String PASSWORD_PROP_NAME = "javax.persistence.jdbc.password";
    private static final String USERNAME_PROP_NAME = "javax.persistence.jdbc.user";

    private static final String H2_CONNECTION_URL_PROP_VALUE = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String H2_DRIVER_CLASS_PROP_VALUE = "org.h2.Driver";
    private static final String SQLITE_CONNECTION_URL_PROP_PREFIX = "jdbc:sqlite:";
    private static final String SQLITE_DRIVER_CLASS_PROP_VALUE = "org.sqlite.JDBC";
    private static final String PASSWORD_PROP_VALUE = "test";
    private static final String USERNAME_PROP_VALUE = "test";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private IDatabaseConnection connection;

    @After
    public void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }

        connection = null;
    }

    @Test
    public void testOpenConnectionToH2DbHavingAllSupportedPersistenceProperties() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME, H2_CONNECTION_URL_PROP_VALUE);
        props.put(DRIVER_CLASS_PROP_NAME, H2_DRIVER_CLASS_PROP_VALUE);
        props.put(USERNAME_PROP_NAME, USERNAME_PROP_VALUE);
        props.put(PASSWORD_PROP_NAME, PASSWORD_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        connection = factory.openConnection();

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection, instanceOf(H2Connection.class));
    }

    @Test
    public void testOpenConnectionToH2DbHavingUsernameAndPasswordEncodedIntoConnectionUrl() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME,
                H2_CONNECTION_URL_PROP_VALUE + ";USER=" + USERNAME_PROP_VALUE + ";PASSWORD=" + PASSWORD_PROP_VALUE);
        props.put(DRIVER_CLASS_PROP_NAME, H2_DRIVER_CLASS_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        connection = factory.openConnection();

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection, instanceOf(H2Connection.class));
    }

    @Test
    public void testOpenConnectionUsingUnknownDriver() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(DRIVER_CLASS_PROP_NAME, "unknown");

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        try {
            connection = factory.openConnection();
            fail("JpaTestException is expected");
        } catch (final JpaUnitException e) {
            // THEN
            // JpaTestException is thrown
        }
    }

    @Test
    public void testOpenConnectionToSqliteDbHavingAllSupportedPersistenceProperties() throws Exception {
        // This test is about a fall back functionality
        // GIVEN
        final File dbFile = folder.newFile("test.db");
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME, SQLITE_CONNECTION_URL_PROP_PREFIX + dbFile.getAbsolutePath());
        props.put(DRIVER_CLASS_PROP_NAME, SQLITE_DRIVER_CLASS_PROP_VALUE);
        props.put(USERNAME_PROP_NAME, USERNAME_PROP_VALUE);
        props.put(PASSWORD_PROP_NAME, PASSWORD_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        connection = factory.openConnection();

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection.getClass(), equalTo(DatabaseConnection.class));
    }

    @Test
    public void testOpenConnectionToSqliteDbWithoutHavingUsernameAndPasswordProperties() throws Exception {
        // GIVEN
        final File dbFile = folder.newFile("test.db");
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME, SQLITE_CONNECTION_URL_PROP_PREFIX + dbFile.getAbsolutePath());
        props.put(DRIVER_CLASS_PROP_NAME, SQLITE_DRIVER_CLASS_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        connection = factory.openConnection();

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection.getClass(), equalTo(DatabaseConnection.class));
    }
}
