package eu.drus.jpa.unit.spi;

public interface TestMethodDecorator {

    int getPriority();

    void apply(TestMethodInvocation ctx) throws Exception;
}
