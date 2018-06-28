package eu.drus.jpa.unit.sql.dbunit.ext;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class Oracle10ConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "oracle.jdbc.OracleDriver".equals(driverClass) || "oracle.jdbc.driver.OracleDriver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection) throws DatabaseUnitException {
        Optional<String> schema = discoverSchema(connection);
        // If no schema provided fall back to creating a connection without schema (Backwards compatibility)
        final IDatabaseConnection dbUnitConnection
            = schema.isPresent() ? new DatabaseConnection(connection, schema.get()) : new DatabaseConnection(connection);
        
        dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        return dbUnitConnection;
    }
    
    private static Optional<String> discoverSchema(final Connection connection) {
        try {
            return Optional.of(connection.getSchema());
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
    
}
