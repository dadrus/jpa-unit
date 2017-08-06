package eu.drus.jpa.unit.cucumber;

import static eu.drus.jpa.unit.cucumber.utils.ClassLoaderUtils.tryLoadClassForName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import eu.drus.jpa.unit.api.JpaUnitException;

public final class BeanFactory {

    private BeanFactory() {}

    public static <T> T createBean(final Class<T> clazz) {
        final Class<?> bpClass = tryLoadClassForName("org.apache.deltaspike.core.api.provider.BeanProvider");
        if (bpClass != null) {
            try {
                final Method getContextualReference = bpClass.getMethod("getContextualReference", Class.class, Annotation[].class);
                return clazz.cast(getContextualReference.invoke(null, clazz, new Annotation[0]));
            } catch (final Exception e) {
                // CDI and Deltaspike are not present and configured - fall back
            }
        }

        // TODO: implement lookup for different DI implementations. For now only CDI is supported

        // fall back
        return createInstance(clazz);
    }

    private static <T> T createInstance(final Class<T> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (final NoSuchMethodException e) {
            throw new JpaUnitException(String
                    .format("%s doesn't have an empty constructor. If you need DI, put your DI implementation on the classpath", clazz), e);
        } catch (final Exception e) {
            throw new JpaUnitException(String.format("Failed to instantiate %s", clazz), e);
        }
    }
}
