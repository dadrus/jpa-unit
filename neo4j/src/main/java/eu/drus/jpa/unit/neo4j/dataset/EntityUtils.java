package eu.drus.jpa.unit.neo4j.dataset;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.CaseFormat;

public final class EntityUtils {

    private EntityUtils() {}

    public static List<String> getNamesOfIdProperties(final Class<?> clazz) {
        final List<String> idList = new ArrayList<>();

        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            collectIdNamesFromFields(idList, c);
            collectIdNamesFromMethods(idList, c);
        }

        return idList;
    }

    private static void collectIdNamesFromMethods(final List<String> idList, final Class<?> c) {
        for (final Method method : c.getDeclaredMethods()) {
            if (method.getAnnotation(Id.class) != null) {
                final Column columnAnnotation = method.getAnnotation(Column.class);
                if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
                    idList.add(columnAnnotation.name());
                } else {
                    // name without "get"
                    final String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, method.getName().substring(3));
                    idList.add(propertyName);
                }
            } else if (method.getAnnotation(EmbeddedId.class) != null) {
                final Class<?> fieldType = method.getReturnType();
                // all fields have to be used as a composite ID
                final String propertyName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, method.getName().substring(3));
                for (final Field idField : fieldType.getDeclaredFields()) {
                    idList.add(propertyName + "." + getPropertyName(idField));
                }
            }
        }
    }

    private static void collectIdNamesFromFields(final List<String> idList, final Class<?> clazz) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(Id.class) != null) {
                idList.add(getPropertyName(field));
            } else if (field.getAnnotation(EmbeddedId.class) != null) {
                final Class<?> fieldType = field.getType();
                // all fields have to be used as a composite ID
                for (final Field idField : fieldType.getDeclaredFields()) {
                    idList.add(field.getName() + "." + getPropertyName(idField));
                }
            }
        }
    }

    private static String getPropertyName(final Field field) {
        final Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        } else {
            return field.getName();
        }
    }

    public static Class<?> getEntityClassFromNodeLabels(final List<String> labels, final List<Class<?>> classes)
            throws NoSuchClassException {
        for (final String label : labels) {
            final Optional<Class<?>> classHit = classes.stream().filter(c -> {
                // try to find the class based on its name
                if (c.getName().endsWith(label)) {
                    return true;
                } else {
                    // try to find the class based on the @Table(name) settings
                    final Table annotation = c.getAnnotation(Table.class);
                    return annotation != null && annotation.name().equals(label);
                }
            }).findFirst();

            if (classHit.isPresent()) {
                return classHit.get();
            }
        }

        throw new NoSuchClassException("could not find class for a node with " + labels + " labels.");
    }
}
