package eu.drus.jpa.unit.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ResourceLocatorTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void testLocateProjectLocalResource() throws MalformedURLException, URISyntaxException {
        // GIVEN

        // WHEN
        final URL resource = ResourceLocator.getResource("empty.file");

        // THEN
        assertThat(resource, notNullValue());
        assertThat(new File(resource.toURI()).exists(), equalTo(Boolean.TRUE));
    }

    @Test
    public void testLocateResourceOtsideOfTheProject() throws IOException {
        // GIVEN
        final File tmpFile = tmpFolder.newFile();

        // WHEN
        final URL resource = ResourceLocator.getResource(tmpFile.getPath());

        // THEN
        assertThat(resource, notNullValue());
        assertThat(resource, equalTo(tmpFile.toURI().toURL()));
    }

    @Test
    public void testLocateNotExistentResource() throws MalformedURLException, URISyntaxException {
        // GIVEN

        // WHEN
        final URL resource = ResourceLocator.getResource("some.file");

        // THEN
        assertThat(resource, notNullValue());
        assertThat(new File(resource.toURI()).exists(), equalTo(Boolean.FALSE));
    }
}
