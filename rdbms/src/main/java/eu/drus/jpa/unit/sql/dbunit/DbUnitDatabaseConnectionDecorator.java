package eu.drus.jpa.unit.sql.dbunit;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.sql.Constants;
import eu.drus.jpa.unit.sql.SqlDbConfiguration;

public class DbUnitDatabaseConnectionDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return SqlDbConfiguration.isSupported(ctx.getDescriptor());
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final BasicDataSource ds = (BasicDataSource) ctx.getData(Constants.KEY_DATA_SOURCE);

        final IDatabaseConnection connection = DatabaseConnectionFactory.openConnection(ds);
        ctx.storeData(Constants.KEY_CONNECTION, connection);
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final IDatabaseConnection connection = (IDatabaseConnection) ctx.getData(Constants.KEY_CONNECTION);
        ctx.storeData(Constants.KEY_CONNECTION, null);

        connection.close();
    }

}
