package eu.drus.jpa.unit.mongodb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.FeatureResolver;
import eu.drus.jpa.unit.spi.TestMethodInvocation;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        MongoDbDecorator.class, MongoDbDecoratorTest.class
})
public class MongoDbDecoratorTest {

    @Mock
    private TestMethodInvocation invocation;

    @Mock
    private ExecutionContext ctx;

    @Mock
    private MongoDatabase mongoDataBase;

    @Mock
    private MongoClient mongoClient;

    @Mock
    private MongoDbConfiguration config;

    @Mock
    private MongoDbFeatureExecutor executor;

    private MongoDbDecorator decorator;

    @Before
    public void prepareTest() throws Exception {
        whenNew(MongoDbConfiguration.class).withAnyArguments().thenReturn(config);
        whenNew(FeatureResolver.class).withAnyArguments().thenReturn(null);
        whenNew(MongoDbFeatureExecutor.class).withAnyArguments().thenReturn(executor);

        when(invocation.getContext()).thenReturn(ctx);
        when(ctx.getData(eq(MongoDbDecorator.KEY_MONGO_CLIENT))).thenReturn(mongoClient);
        when(ctx.getData(eq(MongoDbDecorator.KEY_MONGO_DB))).thenReturn(mongoDataBase);
        when(config.createMongoClient()).thenReturn(mongoClient);
        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDataBase);

        decorator = new MongoDbDecorator();
    }

    @Test
    public void testBeforeTest() throws Throwable {
        // GIVEN

        // WHEN
        decorator.beforeTest(invocation);

        // THEN
        verify(executor).executeBeforeTest(eq(mongoDataBase));
        verify(ctx).storeData(eq(MongoDbDecorator.KEY_MONGO_CLIENT), eq(mongoClient));
        verify(ctx).storeData(eq(MongoDbDecorator.KEY_MONGO_DB), eq(mongoDataBase));
    }

    @Test
    public void testAfterTest() throws Throwable {
        // GIVEN
        when(invocation.hasErrors()).thenReturn(Boolean.FALSE);

        // WHEN
        decorator.afterTest(invocation);

        // THEN
        verify(executor).executeAfterTest(eq(mongoDataBase), eq(Boolean.FALSE));
        verify(ctx).storeData(eq(MongoDbDecorator.KEY_MONGO_CLIENT), isNull());
        verify(ctx).storeData(eq(MongoDbDecorator.KEY_MONGO_DB), isNull());
    }

    @Test
    public void testMongoClientIsClosedEvenIfExecuteAfterTestFails() throws Throwable {
        // GIVEN
        when(invocation.hasErrors()).thenReturn(Boolean.TRUE);
        doThrow(RuntimeException.class).when(executor).executeAfterTest(any(MongoDatabase.class), anyBoolean());

        // WHEN
        try {
            decorator.afterTest(invocation);
            fail("Exception expected");
        } catch (final Exception e) {
            // expected
        }

        // THEN
        verify(mongoClient).close();
    }

    @Test
    public void testProcessInstanceDoesNotHaveAnyEffect() throws Exception {
        // GIVEN

        // WHEN
        decorator.processInstance(this, invocation);

        // THEN
        verifyNoMoreInteractions(invocation, mongoDataBase, mongoClient, ctx, executor);
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(4));
    }
}
