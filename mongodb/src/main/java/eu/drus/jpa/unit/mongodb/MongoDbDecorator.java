package eu.drus.jpa.unit.mongodb;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.spi.AbstractDbFeatureFactory;
import eu.drus.jpa.unit.spi.AbstractDbFeatureMethodDecorator;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class MongoDbDecorator extends AbstractDbFeatureMethodDecorator<Document, MongoDatabase> {

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final MongoDbConfiguration config = new MongoDbConfiguration(context.getDescriptor());
        final MongoClient client = new MongoClient(config.getServerAddresses(), config.getCredentials(), config.getClientOptions());
        final MongoDatabase mongoDb = client.getDatabase(config.getDatabaseName());

        context.storeData("mongoClient", client);
        context.storeData("mongoDb", mongoDb);

        beforeTest(invocation, mongoDb);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();

        final MongoClient client = (MongoClient) context.getData("mongoClient");
        final MongoDatabase mongoDb = (MongoDatabase) context.getData("mongoDb");
        context.storeData("mongoClient", null);
        context.storeData("mongoDb", null);

        try {
            afterTest(invocation, mongoDb);
        } finally {
            client.close();
        }
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return MongoDbConfiguration.isSupported(ctx.getDescriptor());
    }

    @Override
    protected AbstractDbFeatureFactory<Document, MongoDatabase> createDbFeatureFactory(final FeatureResolver featureResolver) {
        return new MongoDbFeatureFactory(featureResolver);
    }

}
