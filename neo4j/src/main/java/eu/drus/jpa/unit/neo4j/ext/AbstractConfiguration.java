package eu.drus.jpa.unit.neo4j.ext;

import static eu.drus.jpa.unit.util.ResourceLocator.getResource;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

public abstract class AbstractConfiguration implements Configuration {

    protected String connectionUrl;
    protected String username;
    protected String password;

    protected void loadConfigurationForEmbeddedSetup() throws IOException {
        // an embedded provider can be started with bolt or http url configured
        final Properties props = new Properties();
        props.load(getResource("jpa-unit.properties").openStream());

        connectionUrl = props.getProperty("connection.url");
        username = props.getProperty("user.name");
        password = props.getProperty("user.password");
    }

    @Override
    public DataSource createDataSource() {
        final HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.neo4j.jdbc.Driver");
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setJdbcUrl(connectionUrl);
        ds.setMinimumIdle(1);
        ds.setMinimumIdle(2);

        return ds;
    }
}
