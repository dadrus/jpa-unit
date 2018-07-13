package eu.drus.jpa.unit.sql.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.mysql.MySqlConnection;

public class MySqlConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "com.mysql.cj.jdbc.Driver".equals(driverClass) || "com.mysql.jdbc.Driver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection, final String schema) throws DatabaseUnitException {
        return new MySqlConnection(connection, schema);
    }
}
