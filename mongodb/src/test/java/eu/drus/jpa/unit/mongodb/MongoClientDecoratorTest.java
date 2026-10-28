package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import com.mongodb.MongoClient;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class MongoClientDecoratorTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private MongoClient mongoClient;

    @Mock
    private Configuration configuration;

    @Mock
    private PersistenceUnitDescriptor descriptor;

    @Mock
    private TestInvocation invocation;

    private MongoClientDecorator decorator;

    @Before
    public void prepareTest() {
        try (final MockedConstruction<ConfigurationRegistry> registries = mockConstruction(ConfigurationRegistry.class,
                (mock, context) -> lenient().when(mock.getConfiguration(any(PersistenceUnitDescriptor.class))).thenReturn(configuration))) {
            decorator = new MongoClientDecorator();
        }

        lenient().when(ctx.getData(eq(Constants.KEY_MONGO_CLIENT))).thenReturn(mongoClient);
        lenient().when(invocation.getContext()).thenReturn(ctx);
        lenient().when(ctx.getDescriptor()).thenReturn(descriptor);
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(3));
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN

        // WHEN
        try (final MockedConstruction<MongoClient> clients = mockConstruction(MongoClient.class)) {
            decorator.beforeAll(invocation);

            // THEN
            final MongoClient client = clients.constructed().get(0);
            verify(ctx).storeData(eq(Constants.KEY_MONGO_CLIENT), eq(client));
        }
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.afterAll(invocation);

        // THEN
        verify(mongoClient).close();
        verify(ctx).storeData(eq(Constants.KEY_MONGO_CLIENT), isNull());
    }
}
