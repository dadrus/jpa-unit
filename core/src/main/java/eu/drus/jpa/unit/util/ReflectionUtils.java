package eu.drus.jpa.unit.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {
    private ReflectionUtils() {}
    
    public static void injectValue(final Object obj, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
    	injectValue(obj, obj.getClass().getDeclaredField(fieldName), value);
    }

    public static void injectValue(final Object obj, final Field field, final Object value) throws IllegalAccessException {
        final boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } finally {
            field.setAccessible(isAccessible);
        }
    }
    
    public static Object getValue(final Object src, final String fieldName) throws IllegalAccessException, NoSuchFieldException {
    	return getValue(src, src.getClass().getDeclaredField(fieldName));
    }

    public static Object getValue(final Object src, final Field field) throws IllegalAccessException {
        final boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        try {
            return field.get(src);
        } finally {
            field.setAccessible(isAccessible);
        }
    }
}
