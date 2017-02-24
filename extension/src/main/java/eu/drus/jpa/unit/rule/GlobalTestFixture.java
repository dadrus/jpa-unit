package eu.drus.jpa.unit.rule;

public interface GlobalTestFixture {

    int getPriority();

    void beforeAll(ExecutionContext ctx, Object target) throws Throwable;

    void afterAll(ExecutionContext ctx, Object target) throws Throwable;
}
