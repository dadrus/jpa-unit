package eu.drus.jpa.unit.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public class MongoDbDecorator implements TestMethodDecorator {

    private ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void beforeTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final Configuration configuration = configurationRegistry.getConfiguration(context.getDescriptor());

        final MongoClient client = (MongoClient) context.getData(Constants.KEY_MONGO_CLIENT);
        final MongoDatabase mongoDb = client.getDatabase(configuration.getDatabaseName());
        context.storeData(Constants.KEY_MONGO_DB, mongoDb);

        final MongoDbFeatureExecutor dbFeatureExecutor = new MongoDbFeatureExecutor(invocation.getFeatureResolver());

        dbFeatureExecutor.executeBeforeTest(mongoDb);
        context.storeData(Constants.KEY_FEATURE_EXECUTOR, dbFeatureExecutor);
    }

    @Override
    public void afterTest(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final MongoDatabase mongoDb = (MongoDatabase) context.getData(Constants.KEY_MONGO_DB);
        final MongoDbFeatureExecutor dbFeatureExecutor = (MongoDbFeatureExecutor) context.getData(Constants.KEY_FEATURE_EXECUTOR);
        context.storeData(Constants.KEY_MONGO_DB, null);
        context.storeData(Constants.KEY_FEATURE_EXECUTOR, null);

        dbFeatureExecutor.executeAfterTest(mongoDb, invocation.getException().isPresent());
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return configurationRegistry.hasConfiguration(ctx.getDescriptor());
    }

}
