package eu.drus.jpa.unit.decorator.dbunit;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class DbUnitDecorator implements TestMethodDecorator {

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void apply(final TestMethodInvocation invocation) throws Throwable {
        final FeatureResolver featureResolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(),
                invocation.getTarget().getClass());

        final DbFeatureFactory featureFactory = new DbFeatureFactory(featureResolver);

        final BasicDataSource ds = (BasicDataSource) invocation.getContext().getData("ds");

        final IDatabaseConnection connection = DatabaseConnectionFactory.openConnection(ds);
        try {
            featureFactory.getCleanUpBeforeFeature().execute(connection);
            featureFactory.getCleanupUsingScriptBeforeFeature().execute(connection);
            featureFactory.getApplyCustomScriptBeforeFeature().execute(connection);
            featureFactory.getSeedDataFeature().execute(connection);

            try {
                invocation.proceed();
                featureFactory.getVerifyDataAfterFeature().execute(connection);
            } finally {
                featureFactory.getApplyCustomScriptAfterFeature().execute(connection);
                featureFactory.getCleanupUsingScriptAfterFeature().execute(connection);
                featureFactory.getCleanUpAfterFeature().execute(connection);
            }
        } finally {
            connection.close();
        }
    }
}
