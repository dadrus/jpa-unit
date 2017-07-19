package eu.drus.jpa.unit.spi;

import java.lang.reflect.Method;

public interface TestMethodInvocation {

    Class<?> getTestClass();

    Method getTestMethod();

    Object getTestInstance();

    ExecutionContext getContext();

    boolean hasErrors();

    FeatureResolver getFeatureResolver();
}
