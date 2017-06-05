package eu.drus.jpa.unit.spi;

public interface TestClassDecorator extends TestDecorator {

    boolean isConfigurationSupported(ExecutionContext ctx);

    void beforeAll(ExecutionContext ctx, Class<?> testClass) throws Exception;

    void afterAll(ExecutionContext ctx, Class<?> testClass) throws Exception;
}
