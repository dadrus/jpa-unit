package eu.drus.jpa.unit.spi;

public interface TestMethodDecorator extends TestDecorator {

    void processInstance(Object instance, TestMethodInvocation invocation) throws Exception;

    void beforeTest(TestMethodInvocation invocation) throws Exception;

    void afterTest(TestMethodInvocation invocation) throws Exception;
}
