package eu.drus.jpa.unit.spi;

public interface TestMethodDecorator {

    int getPriority();

    void processInstance(Object instance, TestMethodInvocation invocation) throws Exception;

    void beforeTest(TestMethodInvocation invocation) throws Exception;

    void afterTest(TestMethodInvocation invocation) throws Exception;
}
