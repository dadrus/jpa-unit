package eu.drus.jpa.unit.core.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.netezza.NetezzaDataTypeFactory;

public class NetezzaConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "org.netezza.Driver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection) throws DatabaseUnitException {
        final IDatabaseConnection dbUnitConnection = new DatabaseConnection(connection);
        dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new NetezzaDataTypeFactory());

        return dbUnitConnection;
    }
}
