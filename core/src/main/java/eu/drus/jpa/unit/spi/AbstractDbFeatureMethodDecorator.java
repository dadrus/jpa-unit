package eu.drus.jpa.unit.spi;

public abstract class AbstractDbFeatureMethodDecorator<D, C> implements TestMethodDecorator {

    protected abstract AbstractDbFeatureFactory<D, C> createDbFeatureFactory(FeatureResolver featureResolver);

    protected void beforeTest(final TestMethodInvocation invocation, final C connection) throws DbFeatureException {
        final AbstractDbFeatureFactory<D, C> featureFactory = createDbFeatureFactory(
                new FeatureResolver(invocation.getMethod(), invocation.getTestClass()));

        featureFactory.getCleanUpBeforeFeature().execute(connection);
        featureFactory.getCleanupUsingScriptBeforeFeature().execute(connection);
        featureFactory.getApplyCustomScriptBeforeFeature().execute(connection);
        featureFactory.getSeedDataFeature().execute(connection);
    }

    protected void afterTest(final TestMethodInvocation invocation, final C connection) throws DbFeatureException {
        final AbstractDbFeatureFactory<D, C> featureFactory = createDbFeatureFactory(
                new FeatureResolver(invocation.getMethod(), invocation.getTestClass()));

        try {
            if (!invocation.hasErrors()) {
                featureFactory.getVerifyDataAfterFeature().execute(connection);
            }
        } finally {
            featureFactory.getApplyCustomScriptAfterFeature().execute(connection);
            featureFactory.getCleanupUsingScriptAfterFeature().execute(connection);
            featureFactory.getCleanUpAfterFeature().execute(connection);
        }
    }
}
