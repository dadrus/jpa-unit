package eu.drus.test.persistence.core.dbunit;

import org.dbunit.database.DatabaseConnection;

public interface DbFeature {
    void execute(DatabaseConnection connection) throws DbFeatureException;
}
