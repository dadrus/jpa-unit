package eu.drus.jpa.unit.core;

import java.util.List;

@FunctionalInterface
public interface CleanupStrategyExecutor<C, D> {
    void execute(final C connection, final List<D> initialDataSets, String... tablesToExclude) throws DbFeatureException;
}
