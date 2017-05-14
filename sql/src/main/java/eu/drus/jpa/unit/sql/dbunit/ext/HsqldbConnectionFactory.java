package eu.drus.jpa.unit.sql.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.hsqldb.HsqldbConnection;

public class HsqldbConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "org.hsqldb.jdbc.JDBCDriver".equals(driverClass) || "org.hsqldb.jdbcDriver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection) throws DatabaseUnitException {
        return new HsqldbConnection(connection, null);
    }
}
