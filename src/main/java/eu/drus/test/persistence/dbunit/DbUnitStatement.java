package eu.drus.test.persistence.dbunit;

import org.dbunit.database.DatabaseConnection;
import org.junit.runners.model.Statement;

public class DbUnitStatement extends Statement {

    private final DatabaseConnectionFactory connectionFactory;
    private final DbFeatureFactory featureFactory;
    private final Statement base;

    DbUnitStatement(final DatabaseConnectionFactory connectionFactory, final DbFeatureFactory featureFactory, final Statement base) {
        this.connectionFactory = connectionFactory;
        this.featureFactory = featureFactory;
        this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
        final DatabaseConnection connection = connectionFactory.openConnection();
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
