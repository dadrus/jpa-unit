package eu.drus.jpa.unit.mongodb;

import com.mongodb.MongoClient;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class MongoClientDecorator implements TestClassDecorator {

    private ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return configurationRegistry.hasConfiguration(ctx.getDescriptor());
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final Configuration configuration = configurationRegistry.getConfiguration(ctx.getDescriptor());

        final MongoClient client = new MongoClient(configuration.getServerAddresses(), configuration.getCredentials(),
                configuration.getClientOptions());

        ctx.storeData(Constants.KEY_MONGO_CLIENT, client);
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final MongoClient client = (MongoClient) ctx.getData(Constants.KEY_MONGO_CLIENT);
        ctx.storeData(Constants.KEY_MONGO_CLIENT, null);
        client.close();
    }

}
