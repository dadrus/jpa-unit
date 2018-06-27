package eu.drus.jpa.unit.sql.dbunit.ext;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Oracle10ConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "oracle.jdbc.OracleDriver".equals(driverClass) || "oracle.jdbc.driver.OracleDriver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection) throws DatabaseUnitException {
        try {
            final IDatabaseConnection dbUnitConnection = new DatabaseConnection(connection, connection.getSchema());
            dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
        
            return dbUnitConnection;
        } catch (SQLException e) {
            throw new DatabaseUnitException(e);
        }
    }

}
