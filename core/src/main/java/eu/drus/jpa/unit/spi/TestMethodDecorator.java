package eu.drus.jpa.unit.spi;

public interface TestMethodDecorator extends TestDecorator {

    boolean isConfigurationSupported(ExecutionContext ctx);

    void beforeTest(TestMethodInvocation invocation) throws Exception;

    void afterTest(TestMethodInvocation invocation) throws Exception;
}
