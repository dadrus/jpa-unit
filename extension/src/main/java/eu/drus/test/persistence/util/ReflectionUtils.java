package eu.drus.test.persistence.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    private ReflectionUtils() {}

    public static void injectValue(final Field field, final Object obj, final Object value) throws IllegalAccessException {
        final boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } finally {
            field.setAccessible(isAccessible);
        }
    }
}
