package eu.drus.jpa.unit.decorator.dbunit;

import org.dbunit.database.IDatabaseConnection;

@FunctionalInterface
public interface DbFeature {
    void execute(IDatabaseConnection connection) throws DbFeatureException;
}
