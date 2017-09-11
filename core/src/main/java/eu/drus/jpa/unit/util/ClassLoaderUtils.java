package eu.drus.jpa.unit.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

public final class ClassLoaderUtils {

    private ClassLoaderUtils() {}

    public static Class<?> tryLoadClassForName(final String name) {
        try {
            return loadClassForName(name);
        } catch (final ClassNotFoundException e) {
            // it is a try, so do nothing
            return null;
        }
    }

    public static Class<?> loadClassForName(final String name) throws ClassNotFoundException {
        try {
            return Class.forName(name, false, getClassLoader(null));
        } catch (final ClassNotFoundException e) {
            // fall back
            return Class.forName(name, false, getClassLoader(ClassLoaderUtils.class.getClassLoader()));
        }
    }

    public static ClassLoader getClassLoader(final Object obj) {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new GetClassLoaderAction(obj));
        } else {
            return doGetClassLoader(obj);
        }
    }

    private static ClassLoader doGetClassLoader(final Object obj) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        if (loader == null && obj != null) {
            loader = obj.getClass().getClassLoader();
        }

        if (loader == null) {
            loader = ClassLoaderUtils.class.getClassLoader();
        }

        return loader;
    }

    private static class GetClassLoaderAction implements PrivilegedAction<ClassLoader> {

        private Object obj;

        private GetClassLoaderAction(final Object obj) {
            this.obj = obj;
        }

        @Override
        public ClassLoader run() {
            try {
                return doGetClassLoader(obj);
            } catch (final Exception e) {
                return null;
            }
        }

    }
}