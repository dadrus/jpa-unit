package eu.drus.jpa.unit.decorator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.core.PersistenceUnitDescriptorLoader;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class DataSourceDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Object target) throws Exception {
        @SuppressWarnings("unchecked")
        final Map<String, Object> properties = (Map<String, Object>) ctx.getData("properties");
        final String unitName = (String) ctx.getData("unitName");

        final PersistenceUnitDescriptorLoader pudLoader = new PersistenceUnitDescriptorLoader();
        List<PersistenceUnitDescriptor> descriptors = pudLoader.loadPersistenceUnitDescriptors(properties);

        descriptors = descriptors.stream().filter(u -> unitName.equals(u.getUnitName())).collect(Collectors.toList());

        if (descriptors.isEmpty()) {
            throw new JpaUnitException("No Persistence Unit found for given unit name");
        } else if (descriptors.size() > 1) {
            throw new JpaUnitException("Multiple Persistence Units found for given name");
        }

        final Map<String, Object> dbConfig = descriptors.get(0).getProperties();

        final String driverClass = (String) dbConfig.get("javax.persistence.jdbc.driver");
        final String connectionUrl = (String) dbConfig.get("javax.persistence.jdbc.url");
        final String username = (String) dbConfig.get("javax.persistence.jdbc.user");
        final String password = (String) dbConfig.get("javax.persistence.jdbc.password");

        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClass);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setUrl(connectionUrl);
        ds.setMinIdle(1);
        ds.setMaxIdle(2);

        ctx.storeData("ds", ds);
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Object target) throws Exception {
        final BasicDataSource ds = (BasicDataSource) ctx.getData("ds");
        ds.close();
        ctx.storeData("ds", null);
    }

}
