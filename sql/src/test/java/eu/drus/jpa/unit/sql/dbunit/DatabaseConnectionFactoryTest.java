package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2Connection;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.sql.dbunit.DatabaseConnectionFactory;

// @Ignore("FIXME")
public class DatabaseConnectionFactoryTest {

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
        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(H2_DRIVER_CLASS_PROP_VALUE);
        ds.setUsername(USERNAME_PROP_VALUE);
        ds.setPassword(PASSWORD_PROP_VALUE);
        ds.setUrl(H2_CONNECTION_URL_PROP_VALUE);

        // WHEN
        connection = DatabaseConnectionFactory.openConnection(ds);

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection, instanceOf(H2Connection.class));
    }

    @Test
    public void testOpenConnectionToH2DbHavingUsernameAndPasswordEncodedIntoConnectionUrl() throws ClassNotFoundException {
        // GIVEN
        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(H2_DRIVER_CLASS_PROP_VALUE);
        ds.setUrl(H2_CONNECTION_URL_PROP_VALUE + ";USER=" + USERNAME_PROP_VALUE + ";PASSWORD=" + PASSWORD_PROP_VALUE);

        // WHEN
        connection = DatabaseConnectionFactory.openConnection(ds);

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection, instanceOf(H2Connection.class));
    }

    @Test
    public void testOpenConnectionUsingUnknownDriver() throws ClassNotFoundException {
        // GIVEN
        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("unknown");

        // WHEN
        try {
            connection = DatabaseConnectionFactory.openConnection(ds);
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

        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(SQLITE_DRIVER_CLASS_PROP_VALUE);
        ds.setUsername(USERNAME_PROP_VALUE);
        ds.setPassword(PASSWORD_PROP_VALUE);
        ds.setUrl(SQLITE_CONNECTION_URL_PROP_PREFIX + dbFile.getAbsolutePath());

        // WHEN
        connection = DatabaseConnectionFactory.openConnection(ds);

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection.getClass(), equalTo(DatabaseConnection.class));
    }

    @Test
    public void testOpenConnectionToSqliteDbWithoutHavingUsernameAndPasswordProperties() throws Exception {
        // GIVEN
        final File dbFile = folder.newFile("test.db");

        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(SQLITE_DRIVER_CLASS_PROP_VALUE);
        ds.setUrl(SQLITE_CONNECTION_URL_PROP_PREFIX + dbFile.getAbsolutePath());

        // WHEN
        connection = DatabaseConnectionFactory.openConnection(ds);

        // THEN
        assertThat(connection, notNullValue());
        assertThat(connection.getClass(), equalTo(DatabaseConnection.class));
    }
}
