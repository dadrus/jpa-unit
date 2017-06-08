package eu.drus.jpa.unit.sql.dbunit;

import java.util.Map;

import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;

import eu.drus.jpa.unit.spi.AbstractDbFeatureFactory;
import eu.drus.jpa.unit.spi.AbstractDbFeatureMethodDecorator;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

public class DbUnitDecorator extends AbstractDbFeatureMethodDecorator<IDataSet, IDatabaseConnection> {

    private static final String KEY_CONNECTION = "connection";

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public void processInstance(final Object instance, final TestMethodInvocation invocation) throws Exception {
        // nothing to do
    }

    @Override
    public void beforeTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final BasicDataSource ds = (BasicDataSource) invocation.getContext().getData("ds");

        final IDatabaseConnection connection = DatabaseConnectionFactory.openConnection(ds);
        context.storeData(KEY_CONNECTION, connection);

        beforeTest(invocation, connection);
    }

    @Override
    public void afterTest(final TestMethodInvocation invocation) throws Exception {
        final ExecutionContext context = invocation.getContext();
        final IDatabaseConnection connection = (IDatabaseConnection) context.getData(KEY_CONNECTION);

        try {
            afterTest(invocation, connection);
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        final PersistenceUnitDescriptor descriptor = ctx.getDescriptor();
        final Map<String, Object> dbConfig = descriptor.getProperties();

        return dbConfig.containsKey("javax.persistence.jdbc.driver") && dbConfig.containsKey("javax.persistence.jdbc.url")
                && dbConfig.containsKey("javax.persistence.jdbc.user") && dbConfig.containsKey("javax.persistence.jdbc.password");
    }

    @Override
    protected AbstractDbFeatureFactory<IDataSet, IDatabaseConnection> createDbFeatureFactory(final FeatureResolver featureResolver) {
        return new DbFeatureFactory(featureResolver);
    }
}
