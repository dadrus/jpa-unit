package eu.drus.jpa.unit.core.dbunit;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ServiceLoader;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.JpaUnitException;
import eu.drus.jpa.unit.core.dbunit.ext.DbUnitConnectionFactory;

public class DatabaseConnectionFactory {

    private static final ServiceLoader<DbUnitConnectionFactory> SERVICE_LOADER = ServiceLoader.load(DbUnitConnectionFactory.class);

    private final DataSource dataSource;

    private final String driverClass;

    public DatabaseConnectionFactory(final DataSource dataSource, final String driverClass) {
        this.dataSource = dataSource;
        this.driverClass = driverClass;
    }

    public IDatabaseConnection openConnection() {
        try {
            final Connection connection = dataSource.getConnection();

            for (final DbUnitConnectionFactory impl : SERVICE_LOADER) {
                if (impl.supportsDriver(driverClass)) {
                    return impl.createConnection(connection);
                }
            }

            // fall back if no specific implementation is available
            return new DatabaseConnection(connection);
        } catch (final DatabaseUnitException | SQLException e) {
            throw new JpaUnitException(e);
        }
    }
}
