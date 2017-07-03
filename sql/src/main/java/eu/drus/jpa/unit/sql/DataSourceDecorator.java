package eu.drus.jpa.unit.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class DataSourceDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final SqlDbConfiguration configuration = new SqlDbConfiguration(ctx.getDescriptor());
        ctx.storeData(Constants.KEY_DATA_SOURCE, configuration.createDataSource());
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final BasicDataSource ds = (BasicDataSource) ctx.getData(Constants.KEY_DATA_SOURCE);
        ds.close();
        ctx.storeData(Constants.KEY_DATA_SOURCE, null);
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return SqlDbConfiguration.isSupported(ctx.getDescriptor());
    }

}
