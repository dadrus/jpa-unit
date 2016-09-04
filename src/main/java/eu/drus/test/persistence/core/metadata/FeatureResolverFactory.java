package eu.drus.test.persistence.core.metadata;

import java.lang.reflect.Method;

public class FeatureResolverFactory {

    public FeatureResolver createFeatureResolver(final Method testMethod, final Class<?> clazz) {
        return new FeatureResolver(testMethod, clazz);
    }
}
