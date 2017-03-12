package eu.drus.jpa.unit.spi;

import java.lang.reflect.Method;

public interface TestMethodInvocation {

    void proceed() throws Exception;

    Method getMethod();

    Object getTarget();

    ExecutionContext getContext();
}
