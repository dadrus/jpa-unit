package eu.drus.test.persistence.core.dbunit;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;

import eu.drus.test.persistence.JpaTestException;

public class DatabaseConnectionFactory {

    private Map<String, Object> properties;

    public DatabaseConnectionFactory(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public DatabaseConnection openConnection() {
        final String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
        final String connectionUrl = (String) properties.get("javax.persistence.jdbc.url");
        final String username = (String) properties.get("javax.persistence.jdbc.user");
        final String password = (String) properties.get("javax.persistence.jdbc.password");

        try {
            Class.forName(driverClass);
        } catch (final ClassNotFoundException e) {
            throw new JpaTestException(e);
        }

        try {
            if (username == null && password == null) {
                return new DatabaseConnection(DriverManager.getConnection(connectionUrl));
            } else {
                return new DatabaseConnection(DriverManager.getConnection(connectionUrl, username, password));
            }
        } catch (DatabaseUnitException | SQLException e) {
            throw new JpaTestException(e);
        }
    }
}
