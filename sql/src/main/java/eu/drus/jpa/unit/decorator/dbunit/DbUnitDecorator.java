package eu.drus.jpa.unit.decorator.dbunit;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class DbUnitDecorator implements TestMethodDecorator {

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
        final FeatureResolver featureResolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(),
                invocation.getTestClass());

        final DbFeatureFactory featureFactory = new DbFeatureFactory(featureResolver);

        final BasicDataSource ds = (BasicDataSource) invocation.getContext().getData("ds");

        final IDatabaseConnection connection = DatabaseConnectionFactory.openConnection(ds);
        context.storeData("connection", connection);

        featureFactory.getCleanUpBeforeFeature().execute(connection);
        featureFactory.getCleanupUsingScriptBeforeFeature().execute(connection);
        featureFactory.getApplyCustomScriptBeforeFeature().execute(connection);
        featureFactory.getSeedDataFeature().execute(connection);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final IDatabaseConnection connection = (IDatabaseConnection) context.getData("connection");

        final FeatureResolver featureResolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(),
                invocation.getTestClass());

        final DbFeatureFactory featureFactory = new DbFeatureFactory(featureResolver);

        try {
            if (!invocation.hasErrors()) {
                featureFactory.getVerifyDataAfterFeature().execute(connection);
            }
        } finally {
            try {
                featureFactory.getApplyCustomScriptAfterFeature().execute(connection);
                featureFactory.getCleanupUsingScriptAfterFeature().execute(connection);
                featureFactory.getCleanUpAfterFeature().execute(connection);
            } finally {
                connection.close();
            }
        }
    }
}
