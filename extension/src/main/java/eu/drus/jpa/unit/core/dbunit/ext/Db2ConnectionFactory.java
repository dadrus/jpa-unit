package eu.drus.jpa.unit.core.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.db2.Db2Connection;

public class Db2ConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "com.ibm.db2.jcc.DB2Driver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection) throws DatabaseUnitException {
        return new Db2Connection(connection, null);
    }
}
