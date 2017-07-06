package eu.drus.jpa.unit.mongodb.ext;

import java.util.ServiceLoader;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class ConfigurationRegistry {

    private static final ServiceLoader<ConfigurationFactory> CONFIG_FACTORIES = ServiceLoader.load(ConfigurationFactory.class);

    public boolean hasConfiguration(final PersistenceUnitDescriptor descriptor) {
        for (final ConfigurationFactory factory : CONFIG_FACTORIES) {
            if (factory.isSupported(descriptor)) {
                return true;
            }
        }
        return false;
    }

    public Configuration getConfiguration(final PersistenceUnitDescriptor descriptor) {
        for (final ConfigurationFactory factory : CONFIG_FACTORIES) {
            if (factory.isSupported(descriptor)) {
                return factory.createConfiguration(descriptor);
            }
        }
        throw new JpaUnitException("Unsupported JPA provider");
    }
}
