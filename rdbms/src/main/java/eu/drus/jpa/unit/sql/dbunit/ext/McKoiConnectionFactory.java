package eu.drus.jpa.unit.sql.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.mckoi.MckoiConnection;

public class McKoiConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "com.mckoi.JDBCDriver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection, final String schema) throws DatabaseUnitException {
        return new MckoiConnection(connection, schema);
    }
}
