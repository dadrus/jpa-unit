package eu.drus.jpa.unit.sql.dbunit;

import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;
import eu.drus.jpa.unit.sql.Constants;

public class DbUnitDecorator implements TestMethodDecorator {

    protected static final String KEY_CONNECTION = "eu.drus.jpa.unit.sql.DatabaseConnection";

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final BasicDataSource ds = (BasicDataSource) invocation.getContext().getData(Constants.KEY_DATA_SOURCE);

        final IDatabaseConnection connection = DatabaseConnectionFactory.openConnection(ds);
        context.storeData(KEY_CONNECTION, connection);

        final SqlDbFeatureExecutor dbFeatureExecutor = new SqlDbFeatureExecutor(
                new FeatureResolver(invocation.getMethod(), invocation.getTestClass()));

        dbFeatureExecutor.executeBeforeTest(connection);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final IDatabaseConnection connection = (IDatabaseConnection) context.getData(KEY_CONNECTION);
        context.storeData(KEY_CONNECTION, null);

        final SqlDbFeatureExecutor dbFeatureExecutor = new SqlDbFeatureExecutor(
                new FeatureResolver(invocation.getMethod(), invocation.getTestClass()));

        try {
            dbFeatureExecutor.executeAfterTest(connection, invocation.hasErrors());
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        final PersistenceUnitDescriptor descriptor = ctx.getDescriptor();
        final Map<String, Object> dbConfig = descriptor.getProperties();

        return dbConfig.containsKey("javax.persistence.jdbc.driver") && dbConfig.containsKey("javax.persistence.jdbc.url")
                && dbConfig.containsKey("javax.persistence.jdbc.user") && dbConfig.containsKey("javax.persistence.jdbc.password");
    }
}
