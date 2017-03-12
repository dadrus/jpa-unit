package eu.drus.jpa.unit.util;

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

    public static Object getValue(final Field field, final Object src) throws IllegalAccessException {
        final boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(src);
        } finally {
            field.setAccessible(isAccessible);
        }
    }
}
