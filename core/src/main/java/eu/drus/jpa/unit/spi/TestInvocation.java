package eu.drus.jpa.unit.spi;

import java.lang.reflect.Method;
import java.util.Optional;

public interface TestInvocation {

    Class<?> getTestClass();

    ExecutionContext getContext();

    Optional<Method> getTestMethod();

    Optional<Object> getTestInstance();

    Optional<Throwable> getException();

    FeatureResolver getFeatureResolver();
}
