package eu.drus.jpa.unit.concordion;

import org.concordion.internal.FixtureInstance;
import org.concordion.internal.FixtureSpecificationMapper;
import org.concordion.internal.util.SimpleFormatter;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.DecoratorExecutor;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestInvocation;
import eu.drus.jpa.unit.util.ReflectionUtils;

public class JpaUnitFixture extends FixtureInstance {

    private DecoratorExecutor executor;
    private Object originalFixture;
    private TestInvocation invocation;

    public JpaUnitFixture(final DecoratorExecutor executor, final Object fixtureObject) {
        super(fixtureObject);
        originalFixture = getDelegate(fixtureObject);
        this.executor = executor;

        final FeatureResolver resolver = FeatureResolver.newFeatureResolver(originalFixture.getClass()).build();
        invocation = new TestInvocationImpl(originalFixture.getClass(), resolver);
    }

    @Override
    public void beforeSpecification() {
        try {
            executor.processBeforeAll(invocation);
        } catch (final Exception e) {
            throw new JpaUnitException(e);
        }
        super.beforeSpecification();
    }

    @Override
    public void afterSpecification() {
        try {
            executor.processAfterAll(invocation);
        } catch (final Exception e) {
            throw new JpaUnitException(e);
        }
        super.afterSpecification();
    }

    private static Object getDelegate(final Object fixtureObject) {
    	try {
			return ReflectionUtils.getValue(fixtureObject, "bean");
		} catch (Exception e) {
			throw new JpaUnitException("Internal Error. No ConcordionInterceptor registered. Please submit a bug report!");
		}
    }

    @Override
    public String getSpecificationDescription() {
        final String name = FixtureSpecificationMapper.removeSuffixFromFixtureName(originalFixture.getClass().getSimpleName());
        return SimpleFormatter.format("[Concordion Specification for '%s']", name);
    }
}
