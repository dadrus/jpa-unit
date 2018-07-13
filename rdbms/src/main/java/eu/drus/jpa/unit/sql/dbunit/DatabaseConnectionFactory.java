package eu.drus.jpa.unit.sql.dbunit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ServiceLoader;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.sql.dbunit.ext.DbUnitConnectionFactory;

public final class DatabaseConnectionFactory {

    private static final ServiceLoader<DbUnitConnectionFactory> SERVICE_LOADER = ServiceLoader.load(DbUnitConnectionFactory.class);

    private DatabaseConnectionFactory() {}

    public static IDatabaseConnection openConnection(final BasicDataSource ds) {
        try {
            final Connection connection = ds.getConnection();

            for (final DbUnitConnectionFactory impl : SERVICE_LOADER) {
                if (impl.supportsDriver(ds.getDriverClassName())) {
                    return impl.createConnection(connection, discoverSchema(connection));
                }
            }

            // fall back if no specific implementation is available
            return new DatabaseConnection(connection);
        } catch (final DatabaseUnitException | SQLException e) {
            throw new JpaUnitException(e);
        }
    }

    private static String discoverSchema(final Connection connection) {
        try {
            return connection.getSchema();
        } catch (final SQLException e) {
            return null;
        }
    }
}
