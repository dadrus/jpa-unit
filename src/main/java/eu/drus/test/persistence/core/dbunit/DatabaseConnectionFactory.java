package eu.drus.test.persistence.core.dbunit;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;

public class DatabaseConnectionFactory {

    private Map<String, Object> properties;

    public DatabaseConnectionFactory(final Map<String, Object> properties) {
        this.properties = properties;
    }

    public DatabaseConnection openConnection() throws SQLException, DatabaseUnitException {
        final String connectionUrl = (String) properties.get("javax.persistence.jdbc.url");
        final String driverClass = (String) properties.get("javax.persistence.jdbc.driver");
        final String password = (String) properties.get("javax.persistence.jdbc.password");
        final String username = (String) properties.get("javax.persistence.jdbc.user");

        try {
            Class.forName(driverClass);
        } catch (final ClassNotFoundException e) {
            throw new SQLException(e);
        }

        if (username == null && password == null) {
            return new DatabaseConnection(DriverManager.getConnection(connectionUrl));
        } else {
            return new DatabaseConnection(DriverManager.getConnection(connectionUrl, username, password));
        }
    }
}
