package eu.drus.test.persistence.core.dbunit;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.dbunit.database.DatabaseConnection;
import org.junit.Test;

import eu.drus.test.persistence.JpaUnitException;

public class DatabaseConnectionFactoryTest {

    private static final String CONNECTION_URL_PROP_NAME = "javax.persistence.jdbc.url";
    private static final String DRIVER_CLASS_PROP_NAME = "javax.persistence.jdbc.driver";
    private static final String PASSWORD_PROP_NAME = "javax.persistence.jdbc.password";
    private static final String USERNAME_PROP_NAME = "javax.persistence.jdbc.user";

    private static final String CONNECTION_URL_PROP_VALUE = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
    private static final String DRIVER_CLASS_PROP_VALUE = "org.h2.Driver";
    private static final String PASSWORD_PROP_VALUE = "test";
    private static final String USERNAME_PROP_VALUE = "test";

    @Test
    public void testOpenConnectionHavingAllSupportedPersistenceProperties() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME, CONNECTION_URL_PROP_VALUE);
        props.put(DRIVER_CLASS_PROP_NAME, DRIVER_CLASS_PROP_VALUE);
        props.put(USERNAME_PROP_NAME, USERNAME_PROP_VALUE);
        props.put(PASSWORD_PROP_NAME, PASSWORD_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        final DatabaseConnection connection = factory.openConnection();

        // THEN
        assertThat(connection, notNullValue());
    }

    @Test
    public void testOpenConnectionWithoutHavingUsernameAndPasswordProperties() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME, CONNECTION_URL_PROP_VALUE);
        props.put(DRIVER_CLASS_PROP_NAME, DRIVER_CLASS_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        try {
            factory.openConnection();
            fail("JpaTestException is expected");
        } catch (final JpaUnitException e) {
            // THEN
            // JpaTestException is thrown
        }
    }

    @Test
    public void testOpenConnectionHavingUsernameAndPasswordEncodedIntoConnectionUrl() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(CONNECTION_URL_PROP_NAME,
                CONNECTION_URL_PROP_VALUE + ";USER=" + USERNAME_PROP_VALUE + ";PASSWORD=" + PASSWORD_PROP_VALUE);
        props.put(DRIVER_CLASS_PROP_NAME, DRIVER_CLASS_PROP_VALUE);

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        final DatabaseConnection connection = factory.openConnection();

        // THEN
        assertThat(connection, notNullValue());
    }

    @Test
    public void testOpenConnectionUsingUnknownDriver() throws ClassNotFoundException {
        // GIVEN
        final Map<String, Object> props = new HashMap<>();
        props.put(DRIVER_CLASS_PROP_NAME, "unknown");

        final DatabaseConnectionFactory factory = new DatabaseConnectionFactory(props);

        // WHEN
        try {
            factory.openConnection();
            fail("JpaTestException is expected");
        } catch (final JpaUnitException e) {
            // THEN
            // JpaTestException is thrown
        }
    }
}
