package eu.drus.test.persistence.core.dbunit;

import org.dbunit.database.IDatabaseConnection;

@FunctionalInterface
public interface DbFeature {
    void execute(IDatabaseConnection connection) throws DbFeatureException;
}
