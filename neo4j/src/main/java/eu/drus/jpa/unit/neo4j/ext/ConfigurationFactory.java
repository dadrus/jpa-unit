package eu.drus.jpa.unit.neo4j.ext;

import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public interface ConfigurationFactory {

    boolean isSupported(final PersistenceUnitDescriptor descriptor);

    Configuration createConfiguration(final PersistenceUnitDescriptor descriptor);
}
