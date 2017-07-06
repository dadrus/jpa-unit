package eu.drus.jpa.unit.mongodb.ext;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public interface ConfigurationFactory {

    boolean isSupported(final PersistenceUnitDescriptor descriptor);

    Configuration createConfiguration(final PersistenceUnitDescriptor descriptor);
}
