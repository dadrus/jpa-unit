package eu.drus.jpa.unit.spi;

import java.lang.reflect.Method;

public interface TestMethodInvocation {

    Class<?> getTestClass();

    Method getMethod();

    ExecutionContext getContext();

    boolean hasErrors();
}
