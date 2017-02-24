package eu.drus.jpa.unit.fixture.spi;

public interface TestFixture {

    int getPriority();

    void apply(TestInvocation ctx) throws Throwable;
}
