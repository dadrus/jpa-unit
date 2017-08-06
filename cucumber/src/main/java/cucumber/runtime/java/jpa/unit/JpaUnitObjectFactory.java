package cucumber.runtime.java.jpa.unit;

import static eu.drus.jpa.unit.cucumber.BeanFactory.createBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cucumber.api.java.ObjectFactory;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.cucumber.CucumberInterceptor;
import eu.drus.jpa.unit.cucumber.EqualsInterceptor;
import eu.drus.jpa.unit.cucumber.JpaUnit;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;

public class JpaUnitObjectFactory implements ObjectFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JpaUnitObjectFactory.class);

    private Map<Class<?>, Object> definitions = new HashMap<>();
    private JpaUnit executor = new JpaUnit();

    @Override
    public void start() {
        // nothing to do here
    }

    @Override
    public void stop() {
        for (final Class<?> clazz : definitions.keySet()) {
            try {
                executor.processAfterAll(clazz);
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
        try {
            executor.processBeforeAll(clazz);
        } catch (final Exception e) {
            throw new JpaUnitException("Could not execute beforeAll hook", e);
        }

        final T bean = createBean(clazz);
        final CallbackHelper helper = new CallbackHelper(clazz, new Class[0]) {

            @Override
            protected Object getCallback(final Method method) {
                if (hasCucumberAnnotations(method)) {
                    return new CucumberInterceptor(executor, bean);
                } else if (method.getName().equals("equals")) {
                    return new EqualsInterceptor(bean);
                } else {
                    return (Dispatcher) () -> bean;
                }
            }
        };

        return clazz.cast(Enhancer.create(clazz, new Class[0], helper, helper.getCallbacks()));
    }

    private static boolean hasCucumberAnnotations(final Method method) {
        final Annotation[] annotations = method.getDeclaredAnnotations();
        for (final Annotation annotation : annotations) {
            final Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.getName().startsWith("cucumber.api")) {
                return true;
            }
        }
        return false;
    }
}
