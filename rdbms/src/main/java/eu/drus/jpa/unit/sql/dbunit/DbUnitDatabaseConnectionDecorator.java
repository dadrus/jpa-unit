package eu.drus.jpa.unit.sql.dbunit;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.sql.Constants;
import eu.drus.jpa.unit.sql.SqlDbConfiguration;
import eu.drus.jpa.unit.util.ResourceLocator;

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
    public void beforeAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final BasicDataSource ds = (BasicDataSource) context.getData(Constants.KEY_DATA_SOURCE);

        final IDatabaseConnection connection = DatabaseConnectionFactory.openConnection(ds);
        connection.getConfig().setPropertiesByString(
                DbUnitConfigurationLoader.loadConfiguration(ResourceLocator.getResource("dbunit.properties")));
        context.storeData(Constants.KEY_CONNECTION, connection);
    }

    @Override
    public void afterAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final IDatabaseConnection connection = (IDatabaseConnection) context.getData(Constants.KEY_CONNECTION);
        context.storeData(Constants.KEY_CONNECTION, null);

        connection.close();
    }

}
