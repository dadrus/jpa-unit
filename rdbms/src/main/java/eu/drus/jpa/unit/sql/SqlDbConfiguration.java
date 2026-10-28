package eu.drus.jpa.unit.sql;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import eu.drus.jpa.unit.spi.PersistenceProperties;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

public class SqlDbConfiguration {

    private Map<String, Object> dbConfig;

    public SqlDbConfiguration(final PersistenceUnitDescriptor descriptor) {
        dbConfig = descriptor.getProperties();
    }

    public static boolean isSupported(final PersistenceUnitDescriptor descriptor) {
        final Map<String, Object> dbConfig = descriptor.getProperties();

        return PersistenceProperties.containsKey(dbConfig, "jdbc.driver") && PersistenceProperties.containsKey(dbConfig, "jdbc.url");
    }

    public DataSource createDataSource() {
        final String driverClass = (String) PersistenceProperties.get(dbConfig, "jdbc.driver");
        final String connectionUrl = (String) PersistenceProperties.get(dbConfig, "jdbc.url");
        final String username = (String) PersistenceProperties.get(dbConfig, "jdbc.user");
        final String password = (String) PersistenceProperties.get(dbConfig, "jdbc.password");

        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClass);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setUrl(connectionUrl);
        ds.setMinIdle(1);
        ds.setMaxIdle(2);

        return ds;
    }
}
