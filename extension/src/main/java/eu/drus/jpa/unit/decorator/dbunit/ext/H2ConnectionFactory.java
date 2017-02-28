package eu.drus.jpa.unit.decorator.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2Connection;

public class H2ConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "org.h2.Driver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection) throws DatabaseUnitException {
        return new H2Connection(connection, null);
    }
}
