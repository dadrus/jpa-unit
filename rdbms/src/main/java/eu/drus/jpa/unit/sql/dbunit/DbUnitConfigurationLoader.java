package eu.drus.jpa.unit.sql.dbunit;

import java.net.URL;
import java.util.Properties;

public final class DbUnitConfigurationLoader {

    private DbUnitConfigurationLoader() {}

    public static Properties loadConfiguration(final URL resourceUrl) {
        final Properties properties = new Properties();

        try {
            if(resourceUrl != null) {
                properties.load(resourceUrl.openStream());
            }
        } catch(final Exception e) {
            // ignore
        }

        return properties;
    }
}
