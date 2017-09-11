package eu.drus.jpa.unit.api.concordion;

import static eu.drus.jpa.unit.util.ClassLoaderUtils.tryLoadClassForName;
import static eu.drus.jpa.unit.util.ReflectionUtils.getValue;
import static eu.drus.jpa.unit.util.ReflectionUtils.injectValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.concordion.api.Fixture;
import org.concordion.api.Resource;
import org.concordion.api.SpecificationLocator;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.internal.ClassNameAndTypeBasedSpecificationLocator;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.concordion.ConcordionInterceptor;
import eu.drus.jpa.unit.concordion.EqualsInterceptor;
import eu.drus.jpa.unit.concordion.JpaUnitFixture;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackHelper;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

public class JpaUnitConcordionRunner extends ConcordionRunner {

    private static final Logger LOG = LoggerFactory.getLogger(JpaUnitConcordionRunner.class);

    private static DecoratorExecutor executor = new DecoratorExecutor();

    public JpaUnitConcordionRunner(final Class<?> fixtureClass) throws InitializationError {
        super(fixtureClass);
    }

    private static Object getDelegate(final Object fixtureObject) {
        final Callback[] callbacks = ((Factory) fixtureObject).getCallbacks();
        for (final Callback callback : callbacks) {
            if (callback instanceof ConcordionInterceptor) {
                return ((ConcordionInterceptor) callback).getDelegate();
            }
        }

        throw new JpaUnitException("Internal Error. No ConcordionInterceptor registered. Please submit a bug report!");
    }

    @Override
    protected Object createTest() throws Exception {
        Object enhancedFixture;
        final Field firstTestSuperField = getClass().getSuperclass().getDeclaredField("firstTest");
        final Field setupFixtureField = getClass().getSuperclass().getDeclaredField("setupFixture");

        final Fixture setupFixture = (Fixture) getValue(setupFixtureField, this);
        final boolean isFirstTest = (boolean) getValue(firstTestSuperField, this);

        if (isFirstTest) {
            injectValue(firstTestSuperField, this, false);
            // we've already created a test object above, so reuse it to make sure we don't
            // initialize the fixture object multiple times
            enhancedFixture = setupFixture.getFixtureObject();

            // we need to setup the concordion scoped objects so that the @Before methods and @Rules
            // can access them
            final Object fixtureObject = getDelegate(enhancedFixture);
            setupFixture.setupForRun(fixtureObject);

            injectFields(fixtureObject);
        } else {
            // junit creates a new object for each test case, so we need to capture this
            // and setup our object - that makes sure that scoped variables are injected properly
            // the setup of concordion scoped objects is done in this call
            final Object fixtureObject = super.createTest();
            injectFields(fixtureObject);
            enhancedFixture = createProxy(fixtureObject);
        }

        return enhancedFixture;
    }

    private void injectFields(final Object fixtureObject) {
        final Class<?> bpClass = tryLoadClassForName("org.apache.deltaspike.core.api.provider.BeanProvider");
        if (bpClass != null) {
            try {
                final Method injectFieldsMethod = bpClass.getMethod("injectFields", Object.class);
                injectFieldsMethod.invoke(bpClass, fixtureObject);
            } catch (final Exception e) {
                LOG.error("CDI and Deltaspike are not properly configured", e);
            }
        }

        // XXX: implement lookup for different DI implementations. For now only CDI is supported
    }

    @Override
    protected Fixture createFixture(final Object fixtureObject) {
        if (fixtureObject instanceof Factory) {
            return new JpaUnitFixture(executor, fixtureObject);
        } else {
            return new JpaUnitFixture(executor, createProxy(fixtureObject));
        }
    }

    private Object createProxy(final Object bean) {
        final CallbackHelper helper = new CallbackHelper(bean.getClass(), new Class[0]) {

            @Override
            protected Object getCallback(final Method method) {
                if (method.getName().equals("equals")) {
                    return new EqualsInterceptor(bean);
                } else {
                    return new ConcordionInterceptor(executor, bean);
                }
            }
        };

        return Enhancer.create(bean.getClass(), new Class[0], helper, helper.getCallbacks());
    }

    @Override
    protected SpecificationLocator getSpecificationLocator() {
        return new ClassNameAndTypeBasedSpecificationLocator() {
            @Override
            public Resource locateSpecification(final Object fixtureObject, final String typeSuffix) {
                return super.locateSpecification(getDelegate(fixtureObject), typeSuffix);
            }
        };
    }
}
