package eu.drus.jpa.unit.mongodb;

import com.mongodb.MongoClient;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;

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
    public void beforeAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final Configuration configuration = configurationRegistry.getConfiguration(context.getDescriptor());

        final MongoClient client = new MongoClient(configuration.getServerAddresses(), configuration.getCredentials(),
                configuration.getClientOptions());

        context.storeData(Constants.KEY_MONGO_CLIENT, client);
    }

    @Override
    public void afterAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final MongoClient client = (MongoClient) context.getData(Constants.KEY_MONGO_CLIENT);
        context.storeData(Constants.KEY_MONGO_CLIENT, null);
        client.close();
    }

}
