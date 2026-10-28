package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class MongoDbDecoratorTest {

    @Mock
    private TestInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private MongoDatabase mongoDataBase;

    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDbFeatureExecutor executor;

    @Mock
    private Configuration configuration;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    private MongoDbDecorator decorator;

    @Before
    public void prepareTest() {
        try (final MockedConstruction<ConfigurationRegistry> registries = mockConstruction(ConfigurationRegistry.class,
                (mock, context) -> lenient().when(mock.getConfiguration(any(PersistenceUnitDescriptor.class))).thenReturn(configuration))) {
            decorator = new MongoDbDecorator();
        }

        lenient().when(invocation.getContext()).thenReturn(ctx);
        lenient().when(ctx.getDescriptor()).thenReturn(descriptor);
        lenient().when(ctx.getData(eq(Constants.KEY_MONGO_CLIENT))).thenReturn(mongoClient);
        lenient().when(ctx.getData(eq(Constants.KEY_MONGO_DB))).thenReturn(mongoDataBase);
        lenient().when(ctx.getData(eq(Constants.KEY_FEATURE_EXECUTOR))).thenReturn(executor);
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN
        when(configuration.getDatabaseName()).thenReturn("test-db");
        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDataBase);

        // WHEN
        try (final MockedConstruction<MongoDbFeatureExecutor> executors = mockConstruction(MongoDbFeatureExecutor.class)) {
            decorator.beforeTest(invocation);

            // THEN
            final MongoDbFeatureExecutor dbFeatureExecutor = executors.constructed().get(0);
            verify(dbFeatureExecutor).executeBeforeTest(eq(mongoDataBase));
            verify(ctx).storeData(eq(Constants.KEY_MONGO_DB), eq(mongoDataBase));
            verify(ctx).storeData(eq(Constants.KEY_FEATURE_EXECUTOR), eq(dbFeatureExecutor));
        }
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN
        when(invocation.getException()).thenReturn(Optional.empty());

        // WHEN
        decorator.afterTest(invocation);

        // THEN
        verify(executor).executeAfterTest(eq(mongoDataBase), eq(Boolean.FALSE));
        verify(ctx).storeData(eq(Constants.KEY_MONGO_DB), isNull());
        verify(ctx).storeData(eq(Constants.KEY_FEATURE_EXECUTOR), isNull());
    }

    @Test
    public void testMongoClientIsClosedEvenIfExecuteAfterTestFails() throws Throwable {
        // GIVEN
        when(invocation.getException()).thenReturn(Optional.of(new Exception()));
        doThrow(RuntimeException.class).when(executor).executeAfterTest(any(MongoDatabase.class), anyBoolean());

        // WHEN
        try {
            decorator.afterTest(invocation);
            fail("Exception expected");
        } catch (final Exception e) {
            // expected
        }

        // THEN
        verifyNoInteractions(mongoClient);
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(3));
    }
}
