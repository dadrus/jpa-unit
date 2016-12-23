package eu.drus.jpa.unit.rule.context;

import java.lang.reflect.Field;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class PersistenceContextRule implements MethodRule {

    private final EntityManagerFactoryProducer emfProducer;
    private final Field persistenceField;

    public PersistenceContextRule(final EntityManagerFactoryProducer emfProducer, final Field persistenceField) {
        this.emfProducer = emfProducer;
        this.persistenceField = persistenceField;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new PersistenceContextStatement(emfProducer, persistenceField, base, target);
    }

}
