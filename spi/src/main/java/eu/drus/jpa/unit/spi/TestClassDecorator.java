package eu.drus.jpa.unit.spi;

public interface TestClassDecorator {

    int getPriority();

    void beforeAll(ExecutionContext ctx, Class<?> testClass) throws Exception;

    void afterAll(ExecutionContext ctx, Class<?> testClass) throws Exception;
}
