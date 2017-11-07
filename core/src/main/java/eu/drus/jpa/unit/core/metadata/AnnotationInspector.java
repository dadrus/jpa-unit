package eu.drus.jpa.unit.core.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnotationInspector<T extends Annotation> {

    private final Class<?> testClass;
    private final Class<T> annotationClass;
    private final Map<Method, T> annotatedMethods;
    private final Map<Field, T> annotatedFields;

    AnnotationInspector(final Class<?> testClass, final Class<T> annotationClass) {
        this.testClass = testClass;
        this.annotationClass = annotationClass;
        annotatedMethods = new HashMap<>();
        annotatedFields = new HashMap<>();

        final List<Class<?>> allClasses = getAllClasses(testClass);
        for (final Class<?> clazz : allClasses) {
            annotatedMethods.putAll(fetchMethods(clazz, annotationClass));
            annotatedFields.putAll(fetchFields(clazz, annotationClass));
        }
    }

    private static List<Class<?>> getAllClasses(final Class<?> testClass) {
        final ArrayList<Class<?>> results = new ArrayList<>();
        Class<?> current = testClass;
        while (current != null) {
            results.add(current);
            current = current.getSuperclass();
        }
        return results;
    }

    private Map<Method, T> fetchMethods(final Class<?> clazz, final Class<T> annotation) {
        final Map<Method, T> map = new HashMap<>();

        for (final Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                map.put(method, method.getAnnotation(annotation));
            }
        }

        return map;
    }

    private Map<Field, T> fetchFields(final Class<?> clazz, final Class<T> annotation) {
        final Map<Field, T> map = new HashMap<>();

        for (final Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                map.put(field, field.getAnnotation(annotation));
            }
        }

        return map;
    }

    public Collection<T> fetchAll() {
        final Set<T> all = new HashSet<>();
        all.addAll(annotatedMethods.values());
        all.addAll(annotatedFields.values());
        final T annotationOnClassLevel = getAnnotationOnClassLevel();
        if (annotationOnClassLevel != null) {
            all.add(annotationOnClassLevel);
        }
        return all;
    }

    public boolean isDefinedOnMethod(final Method method) {
        return fetchFromMethod(method) != null;
    }

    public T fetchFromMethod(final Method method) {
        return method != null ? annotatedMethods.get(method) : null;
    }

    public boolean isDefinedOnAnyMethod() {
        return !annotatedMethods.isEmpty();
    }

    public List<Method> getAnnotatedMethods() {
        return new ArrayList<>(annotatedMethods.keySet());
    }

    public boolean isDefinedOnField(final Field field) {
        return fetchFromField(field) != null;
    }

    public T fetchFromField(final Field field) {
        return annotatedFields.get(field);
    }

    public boolean isDefinedOnAnyField() {
        return !annotatedFields.isEmpty();
    }

    public List<Field> getAnnotatedFields() {
        return new ArrayList<>(annotatedFields.keySet());
    }

    public boolean isDefinedOnClassLevel() {
        return getAnnotationOnClassLevel() != null;
    }

    public T getAnnotationOnClassLevel() {
        return testClass.getAnnotation(annotationClass);
    }

    /**
     * Fetches annotation for a given test class. If annotation is defined on method level it's
     * returned as a result. Otherwise class level annotation is returned if present.
     *
     * @return T annotation or null if not found.
     */
    public T fetchUsingFirst(final Method testMethod) {
        T usedAnnotation = getAnnotationOnClassLevel();
        if (isDefinedOnMethod(testMethod)) {
            usedAnnotation = fetchFromMethod(testMethod);
        }

        return usedAnnotation;
    }
}
