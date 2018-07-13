package eu.drus.jpa.unit.sql.dbunit.ext;

import java.sql.Connection;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.netezza.NetezzaDataTypeFactory;
import org.dbunit.ext.netezza.NetezzaMetadataHandler;

public class NetezzaConnectionFactory implements DbUnitConnectionFactory {

    @Override
    public boolean supportsDriver(final String driverClass) {
        return "org.netezza.Driver".equals(driverClass);
    }

    @Override
    public IDatabaseConnection createConnection(final Connection connection, final String schema) throws DatabaseUnitException {
        final IDatabaseConnection dbUnitConnection = new DatabaseConnection(connection, schema);

        dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new NetezzaDataTypeFactory());
        dbUnitConnection.getConfig().setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new NetezzaMetadataHandler());

        return dbUnitConnection;
    }
}
