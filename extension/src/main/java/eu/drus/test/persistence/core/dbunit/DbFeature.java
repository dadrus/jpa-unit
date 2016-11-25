package eu.drus.test.persistence.core.dbunit;

import org.dbunit.database.DatabaseConnection;

@FunctionalInterface
public interface DbFeature {
    void execute(DatabaseConnection connection) throws DbFeatureException;
}
