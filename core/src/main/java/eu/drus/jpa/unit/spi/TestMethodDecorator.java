package eu.drus.jpa.unit.spi;

public interface TestMethodDecorator extends TestDecorator {

    boolean isConfigurationSupported(ExecutionContext ctx);

    void beforeTest(TestInvocation invocation) throws Exception;

    void afterTest(TestInvocation invocation) throws Exception;
}
