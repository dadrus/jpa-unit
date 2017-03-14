package eu.drus.jpa.unit.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class JpaUnitExtension
        implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, TestInstancePostProcessor {

    private static final ServiceLoader<TestClassDecorator> GLOBAL_FIXTURES = ServiceLoader.load(TestClassDecorator.class);

    private static List<TestClassDecorator> getGlobalTestFixtureRules(final ExecutionContext ctx) {
        final List<TestClassDecorator> fixtures = new ArrayList<>();
        GLOBAL_FIXTURES.iterator().forEachRemaining(fixtures::add);
        return fixtures;
    }

    @Override
    public void beforeAll(final ContainerExtensionContext context) throws Exception {
        System.out.println("beforeAll");
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final List<TestClassDecorator> globalFixtures = getGlobalTestFixtureRules(ctx);

        globalFixtures.stream().sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() < b.getPriority() ? 1 : -1)
                .forEach(d -> {
                    try {
                        d.beforeAll(ctx, null);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                });

    }

    @Override
    public void afterAll(final ContainerExtensionContext context) throws Exception {
        System.out.println("afterAll");
        final JpaUnitContext ctx = JpaUnitContext.getInstance(context.getTestClass().get());

        final List<TestClassDecorator> globalFixtures = getGlobalTestFixtureRules(ctx);

        globalFixtures.stream().sorted((a, b) -> a.getPriority() == b.getPriority() ? 0 : a.getPriority() > b.getPriority() ? 1 : -1)
                .forEach(d -> {
                    try {
                        d.afterAll(ctx, null);
                    } catch (final Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public void beforeEach(final TestExtensionContext context) throws Exception {
        System.out.println("beforeEach");
    }

    @Override
    public void afterEach(final TestExtensionContext context) throws Exception {
        System.out.println("afterEach");
    }

    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        System.out.println("postProcessTestInstance");
    }
}
