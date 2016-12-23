package eu.drus.jpa.unit.core.dbunit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.ServiceLoader;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.JpaUnitException;
import eu.drus.jpa.unit.core.dbunit.ext.DbUnitConnectionFactory;

public class DatabaseConnectionFactory {

    private static final ServiceLoader<DbUnitConnectionFactory> SERVICE_LOADER = ServiceLoader.load(DbUnitConnectionFactory.class);

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
        final Connection connection = openConnection(connectionUrl, username, password);

        try {
            for (final DbUnitConnectionFactory impl : SERVICE_LOADER) {
                if (impl.supportsDriver(driverClass)) {
                    return impl.createConnection(connection);
                }
            }

            // fall back if no specific implementation is available
            return new DatabaseConnection(connection);
        } catch (final DatabaseUnitException e) {
            throw new JpaUnitException(e);
        }
    }

    private Connection openConnection(final String connectionUrl, final String username, final String password) {
        try {
            if (username == null && password == null) {
                return DriverManager.getConnection(connectionUrl);
            } else {
                return DriverManager.getConnection(connectionUrl, username, password);
            }
        } catch (final SQLException e) {
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
