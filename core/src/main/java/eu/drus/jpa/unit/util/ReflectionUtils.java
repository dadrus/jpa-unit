package eu.drus.jpa.unit.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {
    private ReflectionUtils() {}
    
    private static Field findField(final Object src, final String fieldName) throws NoSuchFieldException {
		Class<?> current = src.getClass();
    	Field field = null;
    	
    	while(current != null && field == null) {
    		field = getField(current, fieldName);
    		current = current.getSuperclass();
    	}
    	
    	if(field == null) {
    		throw new NoSuchFieldException(src + " does not declare " + fieldName);
    	}
    	
		return field;
	}

	private static Field getField(Class<?> clazz, final String fieldName) {
		for (Field field : clazz.getDeclaredFields()) {
			if(field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}
    
    public static void injectValue(final Object obj, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
    	injectValue(obj, findField(obj, fieldName), value);
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
    	return getValue(src, findField(src, fieldName));
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
