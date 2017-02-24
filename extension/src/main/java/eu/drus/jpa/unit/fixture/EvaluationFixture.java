package eu.drus.jpa.unit.fixture;

import org.dbunit.database.IDatabaseConnection;

import eu.drus.jpa.unit.core.dbunit.DbFeatureFactory;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.rule.TestFixture;
import eu.drus.jpa.unit.rule.TestInvocation;

public class EvaluationFixture implements TestFixture {

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void apply(final TestInvocation invocation) throws Throwable {
        final FeatureResolver featureResolver = invocation.getContext().createFeatureResolver(invocation.getMethod(),
                invocation.getTarget().getClass());

        final DbFeatureFactory featureFactory = new DbFeatureFactory(featureResolver);

        final IDatabaseConnection connection = invocation.getContext().openConnection();
        try {
            featureFactory.getCleanUpBeforeFeature().execute(connection);
            featureFactory.getCleanupUsingScriptBeforeFeature().execute(connection);
            featureFactory.getApplyCustomScriptBeforeFeature().execute(connection);
            featureFactory.getSeedDataFeature().execute(connection);

            try {
                invocation.proceed();
                featureFactory.getVerifyDataAfterFeature().execute(connection);
            } finally {
                featureFactory.getCleanUpAfterFeature().execute(connection);
                featureFactory.getCleanupUsingScriptAfterFeature().execute(connection);
                featureFactory.getApplyCustomScriptAfterFeature().execute(connection);
            }
        } finally {
            connection.close();
        }
    }

}
