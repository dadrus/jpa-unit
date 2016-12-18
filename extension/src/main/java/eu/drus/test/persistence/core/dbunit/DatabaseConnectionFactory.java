package eu.drus.test.persistence.core.dbunit;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.test.persistence.JpaUnitException;

public class DatabaseConnectionFactory {

    private Map<String, Object> properties;

    public DatabaseConnectionFactory(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public IDatabaseConnection openConnection() {
        final String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
        final String connectionUrl = (String) properties.get("javax.persistence.jdbc.url");
        final String username = (String) properties.get("javax.persistence.jdbc.user");
        final String password = (String) properties.get("javax.persistence.jdbc.password");

        loadDriver(driverClass);

        final IDatabaseConnection connection = createDBUnitDatabaseConnection(connectionUrl, username, password);

        return connection;
    }

    private DatabaseConnection createDBUnitDatabaseConnection(final String connectionUrl, final String username, final String password) {
        try {
            if (username == null && password == null) {
                return new DatabaseConnection(DriverManager.getConnection(connectionUrl));
            } else {
                return new DatabaseConnection(DriverManager.getConnection(connectionUrl, username, password));
            }
        } catch (DatabaseUnitException | SQLException e) {
            throw new JpaUnitException(e);
        }
    }

    private void loadDriver(final String driverClass) {
        try {
            Class.forName(driverClass);
        } catch (final ClassNotFoundException e) {
            throw new JpaUnitException(e);
        }
    }
}
