package cucumber.runtime.java.jpa.unit;

import static eu.drus.jpa.unit.cucumber.BeanFactory.createBean;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.ObjectFactory;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.cucumber.EnhancedProxy;
import eu.drus.jpa.unit.cucumber.TestInvocationImpl;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;

public class JpaUnitObjectFactory implements ObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JpaUnitObjectFactory.class);

    private Map<Class<?>, Object> definitions = new HashMap<>();
    private DecoratorExecutor executor = new DecoratorExecutor();

    private Map<Class<?>, TestInvocation> invocationsMap;

    @Override
    public void start() {
        // nothing to do
    }

    @Override
    public void stop() {
        for (final Class<?> clazz : definitions.keySet()) {
            try {
                executor.processAfterAll(invocationsMap.get(clazz));
            } catch (final Exception e) {
                LOG.error("Failed to run after all hook", e);
            }
        }
        definitions.clear();
    }

    @Override
    public boolean addClass(final Class<?> glueClass) {
        return true;
    }

    @Override
    public <T> T getInstance(final Class<T> clazz) {
        if (!definitions.containsKey(clazz)) {
            definitions.put(clazz, createProxy(clazz));
        }
        return clazz.cast(definitions.get(clazz));
    }

    private <T> T createProxy(final Class<T> clazz) {
        if (invocationsMap == null) {
            invocationsMap = new HashMap<>();
        }

        try {
            final FeatureResolver resolver = FeatureResolver.newFeatureResolver(clazz).build();
            final TestInvocation invocation = new TestInvocationImpl(clazz, resolver);
            invocationsMap.put(clazz, invocation);

            executor.processBeforeAll(invocation);
        } catch (final Exception e) {
            throw new JpaUnitException("Could not execute beforeAll hook", e);
        }

        return clazz.cast(EnhancedProxy.create(createBean(clazz), executor));
    }
}
