package eu.drus.jpa.unit.neo4j.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.rules.TemporaryFolder;

public class TestArchive extends TemporaryFolder {

    private static ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

    private URLClassLoader classLoader;

    private TestArchive(final JavaArchive archive) {
        try {
            create();
            final File testPackage = newFile("jpa-unit-test-archive.jar");
            archive.as(ZipExporter.class).exportTo(testPackage, true);
            final URL url = testPackage.toURI().toURL();
            classLoader = new URLClassLoader(new URL[] {
                    url
            }, originalClassLoader);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create TestArchive", e);
        }
    }

    public static Builder newTestArchive() {
        return new Builder();
    }

    @Override
    public void before() throws Throwable {
        super.before();
        Thread.currentThread().setContextClassLoader(classLoader);
    }

    @Override
    public void after() {
        // reset the classloader
        Thread.currentThread().setContextClassLoader(originalClassLoader);
        try {
            classLoader.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        super.after();
    }

    public static class Builder {
        private final JavaArchive archive;

        private Builder() {
            archive = ShrinkWrap.create(JavaArchive.class);
        }

        public Builder addManifestResource(final String resourceName, final String target) {
            archive.addAsManifestResource(resourceName, target);
            return this;
        }

        public Builder addManifestResource(final File resource) {
            archive.addAsManifestResource(resource);
            return this;
        }

        public Builder addManifestResource(final File resource, final String target) {
            archive.addAsManifestResource(resource, target);
            return this;
        }

        public Builder addResource(final String resourceName, final String target) {
            archive.addAsResource(resourceName, target);
            return this;
        }

        public Builder addResource(final File resource) {
            archive.addAsResource(resource);
            return this;
        }

        public Builder addResource(final File resource, final String target) {
            archive.addAsResource(resource, target);
            return this;
        }

        public Builder addClass(final Class<?> clazz) {
            archive.addClass(clazz);
            return this;
        }

        public Builder addClasses(final Class<?>... classes) {
            archive.addClasses(classes);
            return this;
        }

        public Builder addPackage(final String pack) {
            archive.addPackage(pack);
            return this;
        }

        public Builder addPackage(final Package pack) {
            archive.addPackage(pack);
            return this;
        }

        public TestArchive build() {
            return new TestArchive(archive);
        }
    }
}
