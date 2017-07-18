package cucumber.runtime;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.ObjectFactory;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.cucumber.JpaUnitHookExecutor;
import eu.drus.jpa.unit.cucumber.JpaUnitInterceptor;
import net.sf.cglib.proxy.Enhancer;

public class JpaUnitObjectFactory implements ObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JpaUnitObjectFactory.class);

    private final Map<Class<?>, Object> definitions = new HashMap<>();
    private final JpaUnitHookExecutor executor = new JpaUnitHookExecutor();

    @Override
    public void start() {
        // nothing to do here
    }

    @Override
    public void stop() {
        for (final Object obj : definitions.values()) {
            try {
                executor.processAfterAll(obj.getClass());
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
            definitions.put(clazz, createInstance(clazz));
        }
        return clazz.cast(definitions.get(clazz));
    }

    private <T> T createInstance(final Class<T> clazz) {
        final JpaUnitInterceptor interceptor = new JpaUnitInterceptor(executor);
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(interceptor);
        final T obj = clazz.cast(enhancer.create());

        try {
            executor.processBeforeAll(obj.getClass());
        } catch (final Exception e) {
            throw new JpaUnitException("Could not execute beforeAll hook", e);
        }

        return obj;
    }
}
