package eu.drus.test.persistence.rule.evaluation;

import org.dbunit.database.IDatabaseConnection;
import org.junit.runners.model.Statement;

import eu.drus.test.persistence.core.dbunit.DatabaseConnectionFactory;
import eu.drus.test.persistence.core.dbunit.DbFeatureFactory;

public class EvaluationStatement extends Statement {

    private final DatabaseConnectionFactory connectionFactory;
    private final DbFeatureFactory featureFactory;
    private final Statement base;

    EvaluationStatement(final DatabaseConnectionFactory connectionFactory, final DbFeatureFactory featureFactory, final Statement base) {
        this.connectionFactory = connectionFactory;
        this.featureFactory = featureFactory;
        this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
        final IDatabaseConnection connection = connectionFactory.openConnection();
        try {
            featureFactory.getCleanUpBeforeFeature().execute(connection);
            featureFactory.getCleanupUsingScriptBeforeFeature().execute(connection);
            featureFactory.getApplyCustomScriptBeforeFeature().execute(connection);
            featureFactory.getSeedDataFeature().execute(connection);

            try {
                base.evaluate();
                featureFactory.getVerifyDataAfterFeature().execute(connection);
            } finally {
                featureFactory.getCleanUpAfterFeature().execute(connection);
                featureFactory.getCleanupUsingScriptAfterFeature().execute(connection);
                featureFactory.getApplyCustomScriptAfterFeature().execute(connection);
            }
        } finally {
            connection.close();
        }
    }
}
