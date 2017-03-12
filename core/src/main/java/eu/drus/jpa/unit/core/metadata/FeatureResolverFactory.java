package eu.drus.jpa.unit.core.metadata;

import java.lang.reflect.Method;

public final class FeatureResolverFactory {
    private FeatureResolverFactory() {}

    public static FeatureResolver createFeatureResolver(final Method testMethod, final Class<?> clazz) {
        return new FeatureResolver(testMethod, clazz);
    }
}
