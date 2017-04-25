package eu.drus.jpa.unit.spi;

public interface TestClassDecorator extends TestDecorator {

    void beforeAll(ExecutionContext ctx, Class<?> testClass) throws Exception;

    void afterAll(ExecutionContext ctx, Class<?> testClass) throws Exception;
}
