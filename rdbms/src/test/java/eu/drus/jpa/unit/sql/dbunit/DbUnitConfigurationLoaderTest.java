package eu.drus.jpa.unit.sql.dbunit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import org.dbunit.database.DatabaseConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DbUnitConfigurationLoaderTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File existingConfigFile;
    private File notExistingConfigFile;

    private Properties expectedConfig;

    @Before
    public void createDbUnitConfiguration() throws IOException {
        existingConfigFile = tmp.newFile();
        notExistingConfigFile = new File(UUID.randomUUID().toString());

        expectedConfig = new Properties();
        expectedConfig.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, "true");
        expectedConfig.setProperty(DatabaseConfig.PROPERTY_BATCH_SIZE, "1000");

        try (FileOutputStream out = new FileOutputStream(existingConfigFile)) {
            expectedConfig.store(out, "dbunit config");
        }
    }

    @Test
    public void testLoadingConfigurationGivenAnExistingFile() throws MalformedURLException {
        // GIVEN
        final URL url = existingConfigFile.toURI().toURL();

        // WHEN
        final Properties configuration = DbUnitConfigurationLoader.loadConfiguration(url);

        // THEN
        assertThat(configuration.getProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES), equalTo("true"));
        assertThat(configuration.getProperty(DatabaseConfig.PROPERTY_BATCH_SIZE), equalTo("1000"));
        assertThat(configuration.keySet().size(), equalTo(2));
    }

    @Test
    public void testLoadingConfigurationGivenANotExistingFile() throws MalformedURLException {
        // GIVEN
        final URL url = notExistingConfigFile.toURI().toURL();

        // WHEN
        final Properties configuration = DbUnitConfigurationLoader.loadConfiguration(url);

        // THEN
        assertTrue(configuration.keySet().isEmpty());
    }
}
