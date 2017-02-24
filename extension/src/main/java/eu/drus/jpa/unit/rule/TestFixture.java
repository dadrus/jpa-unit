package eu.drus.jpa.unit.rule;

public interface TestFixture {

    int getPriority();

    void apply(TestInvocation ctx) throws Throwable;
}
