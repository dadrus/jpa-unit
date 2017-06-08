package eu.drus.jpa.unit.mongodb;

import java.util.Map;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.spi.AbstractDbFeatureFactory;
import eu.drus.jpa.unit.spi.AbstractDbFeatureMethodDecorator;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
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
        final PersistenceUnitDescriptor descriptor = context.getDescriptor();

        final Map<String, Object> dbConfig = descriptor.getProperties();

        final String host = (String) dbConfig.get("hibernate.ogm.datastore.host");
        final String dataBase = (String) dbConfig.get("hibernate.ogm.datastore.database");

        final MongoClient client = new MongoClient(new ServerAddress(host));
        final MongoDatabase mongoDb = client.getDatabase(dataBase);

        context.storeData("mongoClient", client);

        beforeTest(invocation, mongoDb);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final PersistenceUnitDescriptor descriptor = context.getDescriptor();

        final Map<String, Object> dbConfig = descriptor.getProperties();

        final String dataBase = (String) dbConfig.get("hibernate.ogm.datastore.database");
        final MongoClient client = (MongoClient) context.getData("mongoClient");

        final MongoDatabase mongoDb = client.getDatabase(dataBase);

        try {
            afterTest(invocation, mongoDb);
        } finally {
            client.close();
        }
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        final PersistenceUnitDescriptor descriptor = ctx.getDescriptor();
        final Map<String, Object> dbConfig = descriptor.getProperties();

        return dbConfig.containsKey("hibernate.ogm.datastore.database");
    }

    @Override
    protected AbstractDbFeatureFactory<Document, MongoDatabase> createDbFeatureFactory(final FeatureResolver featureResolver) {
        return new MongoDbFeatureFactory(featureResolver);
    }

}
