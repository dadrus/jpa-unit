package eu.drus.jpa.unit.sql.dbunit;

import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.sql.Constants;
import eu.drus.jpa.unit.sql.SqlDbConfiguration;

public class DbUnitTestMethodDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void beforeTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final SqlDbFeatureExecutor dbFeatureExecutor = new SqlDbFeatureExecutor(invocation.getFeatureResolver());
        final IDatabaseConnection connection = (IDatabaseConnection) context.getData(Constants.KEY_CONNECTION);

        dbFeatureExecutor.executeBeforeTest(connection);
        context.storeData(Constants.KEY_FEATURE_EXECUTOR, dbFeatureExecutor);
    }

    @Override
    public void afterTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final SqlDbFeatureExecutor dbFeatureExecutor = (SqlDbFeatureExecutor) context.getData(Constants.KEY_FEATURE_EXECUTOR);
        context.storeData(Constants.KEY_FEATURE_EXECUTOR, null);

        final IDatabaseConnection connection = (IDatabaseConnection) context.getData(Constants.KEY_CONNECTION);

        dbFeatureExecutor.executeAfterTest(connection, invocation.getException().isPresent());
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return SqlDbConfiguration.isSupported(ctx.getDescriptor());
    }
}
