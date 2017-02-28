package eu.drus.jpa.unit.spi;

public interface TestClassDecorator {

    int getPriority();

    void beforeAll(ExecutionContext ctx, Object target) throws Throwable;

    void afterAll(ExecutionContext ctx, Object target) throws Throwable;
}
