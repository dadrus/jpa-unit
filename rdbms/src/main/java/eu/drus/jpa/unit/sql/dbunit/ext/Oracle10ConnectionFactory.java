/*
 *
 */
package eu.drus.jpa.unit.sql.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;

public class Oracle10ConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "oracle.jdbc.OracleDriver".equals(driverClass) || "oracle.jdbc.driver.OracleDriver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection, final String schema) throws DatabaseUnitException {
        final DatabaseConnection dbUnitConnection = new DatabaseConnection(connection, schema);

        dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());

        return dbUnitConnection;
    }
}