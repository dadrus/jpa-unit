package eu.drus.jpa.unit.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;

public class DataSourceDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void beforeAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final SqlDbConfiguration configuration = new SqlDbConfiguration(context.getDescriptor());
        context.storeData(Constants.KEY_DATA_SOURCE, configuration.createDataSource());
    }

    @Override
    public void afterAll(final TestInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final BasicDataSource ds = (BasicDataSource) context.getData(Constants.KEY_DATA_SOURCE);
        ds.close();
        context.storeData(Constants.KEY_DATA_SOURCE, null);
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return SqlDbConfiguration.isSupported(ctx.getDescriptor());
    }

}
