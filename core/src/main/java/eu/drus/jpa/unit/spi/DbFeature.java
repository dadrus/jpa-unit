package eu.drus.jpa.unit.spi;

@FunctionalInterface
public interface DbFeature<T> {
    void execute(T connection) throws DbFeatureException;
}
