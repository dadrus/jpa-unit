package eu.drus.jpa.unit.core;

@FunctionalInterface
public interface DbFeature<T> {
    void execute(T connection) throws DbFeatureException;
}
