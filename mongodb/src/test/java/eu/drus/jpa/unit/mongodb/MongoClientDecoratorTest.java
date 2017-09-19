package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mongodb.MongoClient;

import eu.drus.jpa.unit.mongodb.ext.Configuration;
import eu.drus.jpa.unit.mongodb.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.PersistenceUnitDescriptor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        MongoClientDecorator.class, MongoClientDecoratorTest.class
})
public class MongoClientDecoratorTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private MongoClient mongoClient;

    @Mock
    private Configuration configuration;

    @Mock
    private ConfigurationRegistry configRegistry;

    private MongoClientDecorator decorator;

    @Before
    public void prepareTest() throws Exception {
        whenNew(ConfigurationRegistry.class).withAnyArguments().thenReturn(configRegistry);
        whenNew(MongoClient.class).withAnyArguments().thenReturn(mongoClient);

        when(ctx.getData(eq(Constants.KEY_MONGO_CLIENT))).thenReturn(mongoClient);
        when(configRegistry.getConfiguration(any(PersistenceUnitDescriptor.class))).thenReturn(configuration);

        decorator = new MongoClientDecorator();
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
        decorator.beforeAll(ctx, null);

        // THEN
        verify(ctx).storeData(eq(Constants.KEY_MONGO_CLIENT), eq(mongoClient));
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.afterAll(ctx, null);

        // THEN
        verify(mongoClient).close();
        verify(ctx).storeData(eq(Constants.KEY_MONGO_CLIENT), isNull());
    }
}
