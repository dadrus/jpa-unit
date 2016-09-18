package eu.drus.test.persistence.dbunit;

import org.dbunit.database.DatabaseConnection;

public interface DbFeature {
    void execute(DatabaseConnection connection) throws DbFeatureException;
}
