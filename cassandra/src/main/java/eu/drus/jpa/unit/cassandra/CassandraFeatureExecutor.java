package eu.drus.jpa.unit.cassandra;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.Session;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.cassandra.dataset.CompositeDataSet;
import eu.drus.jpa.unit.cassandra.dataset.DataSet;
import eu.drus.jpa.unit.cassandra.operation.CassandraOperation;
import eu.drus.jpa.unit.spi.AbstractDbFeatureExecutor;
import eu.drus.jpa.unit.spi.CleanupStrategyExecutor;
import eu.drus.jpa.unit.spi.DataSetFormat;
import eu.drus.jpa.unit.spi.DataSetLoader;
import eu.drus.jpa.unit.spi.DbFeature;
import eu.drus.jpa.unit.spi.FeatureResolver;

public class CassandraFeatureExecutor extends AbstractDbFeatureExecutor<DataSet, Session> {

    protected CassandraFeatureExecutor(final FeatureResolver featureResolver) {
        super(featureResolver);
    }

    @Override
    protected List<DataSet> loadDataSets(final List<String> paths) {
        final List<DataSet> dataSets = new ArrayList<>();
        try {
            for (final String path : paths) {
                final File file = new File(toUri(path));
                final DataSetLoader<DataSet> loader = DataSetFormat.inferFromFile(file).select(new DataSetLoaderProvider());
                dataSets.add(loader.load(file));
            }
        } catch (final IOException | URISyntaxException e) {
            throw new JpaUnitException("Could not load initial data sets", e);
        }
        return dataSets;
    }

    private static URI toUri(final String path) throws URISyntaxException {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null) {
            throw new JpaUnitException(path + " not found");
        }

        return url.toURI();
    }

    @Override
    protected DbFeature<Session> createCleanupFeature(final CleanupStrategy cleanupStrategy, final List<DataSet> initialDataSets) {
        return (final Session sessison) -> {
            final CleanupStrategyExecutor<Session, DataSet> executor = cleanupStrategy.provide(new CleanupStrategyProvider());
            executor.execute(sessison, initialDataSets);
        };
    }

    @Override
    protected DbFeature<Session> createApplyCustomScriptFeature(final List<String> scriptPaths) {
        return (final Session sessison) -> {
            // TODO Auto-generated method stub
        };
    }

    @Override
    protected DbFeature<Session> createSeedDataFeature(final DataSeedStrategy dataSeedStrategy, final List<DataSet> initialDataSets) {
        return (final Session sessison) -> {
            final CassandraOperation operation = dataSeedStrategy.provide(new DataSeedStrategyProvider());
            operation.execute(sessison, mergeDataSets(initialDataSets));
        };
    }

    @Override
    protected DbFeature<Session> createVerifyDataAfterFeature(final ExpectedDataSets expectedDataSets) {
        return (final Session sessison) -> {
            // TODO Auto-generated method stub
        };
    }

    private DataSet mergeDataSets(final List<DataSet> initialDataSets) {
        return new CompositeDataSet(initialDataSets);
    }
}
