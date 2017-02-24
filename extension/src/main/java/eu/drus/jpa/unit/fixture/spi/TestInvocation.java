package eu.drus.jpa.unit.fixture.spi;

import java.lang.reflect.Method;

public interface TestInvocation {

    void proceed() throws Throwable;

    Method getMethod();

    Object getTarget();

    ExecutionContext getContext();
}
