package eu.drus.jpa.unit.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public final class ResourceLocator {

    private ResourceLocator() {}

    public static URL getResource(final String resource) throws MalformedURLException {
        final List<ClassLoader> classLoaders = Arrays.asList(Thread.currentThread().getContextClassLoader(),
                ResourceLocator.class.getClassLoader(), ClassLoader.getSystemClassLoader());

        final URL url = getResource(classLoaders, resource);

        return url == null ? new File(resource).toURI().toURL() : url;
    }

    private static URL getResource(final List<ClassLoader> classLoaders, final String resource) {
        for (final ClassLoader cl : classLoaders) {
            final URL url = cl.getResource(resource);
            if (url != null) {
                return url;
            }
        }
        return null;
    }
}
