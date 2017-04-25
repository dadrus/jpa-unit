package eu.drus.jpa.unit.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestMethodDecorator;

public class DecoratorRegistrar {

    private static final ServiceLoader<TestClassDecorator> CLASS_DECORATORS = ServiceLoader.load(TestClassDecorator.class);
    private static final ServiceLoader<TestMethodDecorator> METHOD_DECORATORS = ServiceLoader.load(TestMethodDecorator.class);

    private DecoratorRegistrar() {}

    public static List<TestClassDecorator> getClassDecorators() {
        final List<TestClassDecorator> decorators = new ArrayList<>();
        CLASS_DECORATORS.iterator().forEachRemaining(decorators::add);
        return decorators;
    }

    public static List<TestMethodDecorator> getMethodDecorators() {
        final List<TestMethodDecorator> decorators = new ArrayList<>();
        METHOD_DECORATORS.iterator().forEachRemaining(decorators::add);
        return decorators;
    }
}
