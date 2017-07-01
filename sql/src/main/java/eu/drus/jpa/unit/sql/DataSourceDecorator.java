package eu.drus.jpa.unit.sql;

import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class DataSourceDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final PersistenceUnitDescriptor descriptor = ctx.getDescriptor();

        final Map<String, Object> dbConfig = descriptor.getProperties();

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

        ctx.storeData(Constants.KEY_DATA_SOURCE, ds);
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final BasicDataSource ds = (BasicDataSource) ctx.getData(Constants.KEY_DATA_SOURCE);
        ds.close();
        ctx.storeData(Constants.KEY_DATA_SOURCE, null);
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        final PersistenceUnitDescriptor descriptor = ctx.getDescriptor();
        final Map<String, Object> dbConfig = descriptor.getProperties();

        return dbConfig.containsKey("javax.persistence.jdbc.driver") && dbConfig.containsKey("javax.persistence.jdbc.url")
                && dbConfig.containsKey("javax.persistence.jdbc.user") && dbConfig.containsKey("javax.persistence.jdbc.password");
    }

}
