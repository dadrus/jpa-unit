package eu.drus.jpa.unit.spi;

public interface TestClassDecorator extends TestDecorator {

    boolean isConfigurationSupported(ExecutionContext ctx);

    void beforeAll(TestInvocation invocation) throws Exception;

    void afterAll(TestInvocation invocation) throws Exception;
}
