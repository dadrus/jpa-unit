package eu.drus.jpa.unit.rule;

import java.lang.reflect.Method;

public interface TestInvocation {

    void proceed() throws Throwable;

    Method getMethod();

    Object getTarget();

    ExecutionContext getContext();
}
