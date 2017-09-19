package eu.drus.jpa.unit.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class MongoDbDecorator implements TestMethodDecorator {

    protected static final String KEY_MONGO_DB = "eu.drus.jpa.unit.mongodb.MongoDatabase";
    protected static final String KEY_MONGO_CLIENT = "eu.drus.jpa.unit.mongodb.MongoClient";
    protected static final String KEY_FEATURE_EXECUTOR = "eu.drus.jpa.unit.mongodb.FeatureExecutor";

    private ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final Configuration configuration = configurationRegistry.getConfiguration(context.getDescriptor());

        final MongoClient client = new MongoClient(configuration.getServerAddresses(), configuration.getCredentials(),
                configuration.getClientOptions());
        final MongoDatabase mongoDb = client.getDatabase(configuration.getDatabaseName());

        context.storeData(KEY_MONGO_CLIENT, client);
        context.storeData(KEY_MONGO_DB, mongoDb);

        final MongoDbFeatureExecutor dbFeatureExecutor = new MongoDbFeatureExecutor(invocation.getFeatureResolver());

        dbFeatureExecutor.executeBeforeTest(mongoDb);
        context.storeData(KEY_FEATURE_EXECUTOR, dbFeatureExecutor);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final MongoClient client = (MongoClient) context.getData(KEY_MONGO_CLIENT);
        final MongoDatabase mongoDb = (MongoDatabase) context.getData(KEY_MONGO_DB);
        final MongoDbFeatureExecutor dbFeatureExecutor = (MongoDbFeatureExecutor) context.getData(KEY_FEATURE_EXECUTOR);
        context.storeData(KEY_MONGO_CLIENT, null);
        context.storeData(KEY_MONGO_DB, null);
        context.storeData(KEY_FEATURE_EXECUTOR, null);

        try {
            dbFeatureExecutor.executeAfterTest(mongoDb, invocation.hasErrors());
        } finally {
            client.close();
        }
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return configurationRegistry.hasConfiguration(ctx.getDescriptor());
    }

}
