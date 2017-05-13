package eu.drus.jpa.unit.decorator.mongodb;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.core.PersistenceUnitDescriptorLoader;
import eu.drus.jpa.unit.core.metadata.FeatureResolver;
import eu.drus.jpa.unit.core.metadata.FeatureResolverFactory;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestMethodDecorator;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class MongoDbDecorator implements TestMethodDecorator {

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
        final FeatureResolver featureResolver = FeatureResolverFactory.createFeatureResolver(invocation.getMethod(),
                invocation.getTestClass());

        final MongoDbFeatureFactory featureFactory = new MongoDbFeatureFactory(featureResolver);

        @SuppressWarnings("unchecked")
        final Map<String, Object> properties = (Map<String, Object>) context.getData("properties");
        final String unitName = (String) context.getData("unitName");

        final PersistenceUnitDescriptorLoader pudLoader = new PersistenceUnitDescriptorLoader();
        List<PersistenceUnitDescriptor> descriptors = pudLoader.loadPersistenceUnitDescriptors(properties);

        descriptors = descriptors.stream().filter(u -> unitName.equals(u.getUnitName())).collect(Collectors.toList());

        if (descriptors.isEmpty()) {
            throw new JpaUnitException("No Persistence Unit found for given unit name");
        } else if (descriptors.size() > 1) {
            throw new JpaUnitException("Multiple Persistence Units found for given name");
        }

        final Map<String, Object> dbConfig = descriptors.get(0).getProperties();

        final String host = (String) dbConfig.get("hibernate.ogm.datastore.host");
        final String dataBase = (String) dbConfig.get("hibernate.ogm.datastore.database");

        final MongoClient client = new MongoClient(new ServerAddress(host));
        final MongoDatabase mongoDb = client.getDatabase(dataBase);

        context.storeData("mongoClient", client);

        featureFactory.getCleanUpBeforeFeature().execute(mongoDb);
        featureFactory.getCleanupUsingScriptBeforeFeature().execute(mongoDb);
        featureFactory.getApplyCustomScriptBeforeFeature().execute(mongoDb);
        featureFactory.getSeedDataFeature().execute(mongoDb);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        // TODO Auto-generated method stub

    }

}
