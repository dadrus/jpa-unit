package eu.drus.test.persistence.rule.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.JpaTestException;
import eu.drus.test.persistence.core.PersistenceUnitDescriptor;
import eu.drus.test.persistence.core.PersistenceUnitDescriptorLoader;
import eu.drus.test.persistence.core.dbunit.DatabaseConnectionFactory;
import eu.drus.test.persistence.core.dbunit.DbFeatureFactory;
import eu.drus.test.persistence.core.metadata.FeatureResolver;
import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;

public class EvaluationRule implements MethodRule {

    private final DatabaseConnectionFactory connectionFactory;
    private final FeatureResolverFactory featureResolverFactory;

    public EvaluationRule(final FeatureResolverFactory featureResolverFactory, final PersistenceUnitDescriptorLoader pudLoader,
            final String unitName, final Map<String, Object> properties) {
        this.featureResolverFactory = featureResolverFactory;

        List<PersistenceUnitDescriptor> descriptors;
        try {
            descriptors = pudLoader.loadPersistenceUnitDescriptors(properties);
        } catch (final IOException e) {
            throw new JpaTestException("Error while loading persistence unit descriptors", e);
        }

        descriptors = descriptors.stream().filter(u -> u.getUnitName().equals(unitName)).collect(Collectors.toList());

        if (descriptors.isEmpty()) {
            throw new JpaTestException("No peristence unit found for given unit name");
        }
        if (descriptors.size() > 1) {
            throw new JpaTestException("Multiple persistence units found for given name");
        }

        final PersistenceUnitDescriptor descriptor = descriptors.get(0);

        connectionFactory = new DatabaseConnectionFactory(descriptor.getProperties());
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        final FeatureResolver featureResolver = featureResolverFactory.createFeatureResolver(method.getMethod(), target.getClass());
        try {
            return new EvaluationStatement(connectionFactory, new DbFeatureFactory(featureResolver), base);
        } catch (final IOException e) {
            throw new JpaTestException("Failed to create statement", e);
        }
    }
}
