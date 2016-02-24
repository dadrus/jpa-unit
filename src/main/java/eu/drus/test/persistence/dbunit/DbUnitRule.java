package eu.drus.test.persistence.dbunit;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class DbUnitRule implements MethodRule {

    private final Map<String, Object> properties;

    public DbUnitRule(final EntityManagerFactory entityManagerFactory) {
        final EntityManager tmp = entityManagerFactory.createEntityManager();
        properties = new HashMap<>();
        properties.putAll(tmp.getProperties());
        tmp.close();
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new DbUnitStatement(properties, target.getClass(), method.getMethod(), base);
    }
}
