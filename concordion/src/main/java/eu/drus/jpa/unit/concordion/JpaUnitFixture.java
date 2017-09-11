package eu.drus.jpa.unit.concordion;

import org.concordion.internal.FixtureInstance;
import org.concordion.internal.FixtureSpecificationMapper;
import org.concordion.internal.util.SimpleFormatter;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.core.JpaUnitContext;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;

public class JpaUnitFixture extends FixtureInstance {

    private DecoratorExecutor executor;
    private Object originalFixture;

    public JpaUnitFixture(final DecoratorExecutor executor, final Object fixtureObject) {
        super(fixtureObject);
        originalFixture = getDelegate(fixtureObject);
        this.executor = executor;
    }

    @Override
    public void beforeSpecification() {
        try {
            executor.processBeforeAll(JpaUnitContext.getInstance(originalFixture.getClass()), originalFixture.getClass());
        } catch (final Exception e) {
            throw new JpaUnitException(e);
        }
        super.beforeSpecification();
    }

    @Override
    public void afterSpecification() {
        try {
            executor.processAfterAll(JpaUnitContext.getInstance(originalFixture.getClass()), originalFixture.getClass());
        } catch (final Exception e) {
            throw new JpaUnitException(e);
        }
        super.afterSpecification();
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
    public String getSpecificationDescription() {
        final String name = FixtureSpecificationMapper.removeSuffixFromFixtureName(originalFixture.getClass().getSimpleName());
        return SimpleFormatter.format("[Concordion Specification for '%s']", name);
    }
}
