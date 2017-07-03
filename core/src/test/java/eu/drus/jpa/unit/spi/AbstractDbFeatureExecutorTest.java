package eu.drus.jpa.unit.spi;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.spi.AbstractDbFeatureExecutor.NopFeature;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        AbstractDbFeatureExecutor.class, AbstractDbFeatureExecutorTest.class
})
public class AbstractDbFeatureExecutorTest {

    private static class Connection {}

    private static class Data {}

    private static class DbFeatureExecutor extends AbstractDbFeatureExecutor<Data, Connection> {

        protected DbFeatureExecutor(final FeatureResolver featureResolver) {
            super(featureResolver);
        }

        @Override
        protected List<Data> loadDataSets(final List<String> paths) {
            return null;
        }

        @Override
        protected DbFeature<Connection> createCleanupFeature(final CleanupStrategy cleanupStrategy, final List<Data> initialDataSets) {
            return null;
        }

        @Override
        protected DbFeature<Connection> createApplyCustomScriptFeature(final List<String> scriptPaths) {
            return null;
        }

        @Override
        protected DbFeature<Connection> createSeedDataFeature(final DataSeedStrategy dataSeedStrategy, final List<Data> initialDataSets) {
            return null;
        }

        @Override
        protected DbFeature<Connection> createVerifyDataAfterFeature(final ExpectedDataSets expectedDataSets) {
            return null;
        }

    }

    @Mock
    private FeatureResolver resolver;

    @Mock
    private NopFeature<Connection> dummyFeature;

    private DbFeatureExecutor executor;

    @Before
    public void setupMocks() throws Exception {
        executor = spy(new DbFeatureExecutor(resolver));
        whenNew(NopFeature.class).withAnyArguments().thenReturn(dummyFeature);
    }

    @Test
    public void testCleanUpBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getCleanUpBeforeFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createCleanupFeature(any(CleanupStrategy.class), anyList());
    }

    @Test
    public void testCleanUpBeforeIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getCleanUpBeforeFeature();

        // THEN
        verify(resolver).getSeedData();
        verify(resolver).getCleanupStrategy();
        verify(executor).loadDataSets(anyList());
        verify(executor).createCleanupFeature(any(CleanupStrategy.class), anyList());
    }

    @Test
    public void testCleanUpAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getCleanUpAfterFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createCleanupFeature(any(CleanupStrategy.class), anyList());
    }

    @Test
    public void testCleanUpAfterIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getCleanUpAfterFeature();

        // THEN
        verify(resolver).getSeedData();
        verify(resolver).getCleanupStrategy();
        verify(executor).loadDataSets(anyList());
        verify(executor).createCleanupFeature(any(CleanupStrategy.class), anyList());
    }

    @Test
    public void testCleanupUsingScriptBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getCleanupUsingScriptBeforeFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testCleanupUsingScriptBeforeIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getCleanupUsingScriptBeforeFeature();

        // THEN
        verify(resolver).getCleanupScripts();
        verify(executor).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testCleanupUsingScriptAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getCleanupUsingScriptAfterFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testCleanupUsingScriptAfterIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptAfter()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getCleanupUsingScriptAfterFeature();

        // THEN
        verify(resolver).getCleanupScripts();
        verify(executor).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testApplyCustomScriptBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getApplyCustomScriptBeforeFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testApplyCustomScriptBeforeIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldApplyCustomScriptBefore()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getApplyCustomScriptBeforeFeature();

        // THEN
        verify(resolver).getPreExecutionScripts();
        verify(executor).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testApplyCustomScriptAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getApplyCustomScriptAfterFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testApplyCustomScriptAfterIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldApplyCustomScriptAfter()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getApplyCustomScriptAfterFeature();

        // THEN
        verify(resolver).getPostExecutionScripts();
        verify(executor).createApplyCustomScriptFeature(anyList());
    }

    @Test
    public void testSeedDataIdDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldSeedData()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getSeedDataFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createSeedDataFeature(any(DataSeedStrategy.class), anyList());
    }

    @Test
    public void testSeedDataIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldSeedData()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getSeedDataFeature();

        // THEN
        verify(resolver).getDataSeedStrategy();
        verify(executor).loadDataSets(anyList());
        verify(executor).createSeedDataFeature(any(DataSeedStrategy.class), anyList());
    }

    @Test
    public void testVerifyDataAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.FALSE);

        // WHEN
        final DbFeature<Connection> feature = executor.getVerifyDataAfterFeature();

        // THEN
        assertThat(feature, notNullValue());
        verify(executor, never()).createVerifyDataAfterFeature(any(ExpectedDataSets.class));
    }

    @Test
    public void testVerifyDataAfterIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getVerifyDataAfterFeature();

        // THEN
        verify(resolver).getExpectedDataSets();
        verify(executor).createVerifyDataAfterFeature(any(ExpectedDataSets.class));
    }

    @Test
    public void testExecuteBeforeTest() throws DbFeatureException {
        // GIVEN
        final Connection connection = new Connection();

        // WHEN
        executor.executeBeforeTest(connection);

        // THEN
        final InOrder inOrder = inOrder(executor, dummyFeature);
        inOrder.verify(executor).getCleanUpBeforeFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getCleanupUsingScriptBeforeFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getApplyCustomScriptBeforeFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getSeedDataFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
    }

    @Test
    public void testExecuteAfterSuccessfulTest() throws DbFeatureException {
        // GIVEN
        final Connection connection = new Connection();

        // WHEN
        executor.executeAfterTest(connection, false);

        // THEN
        final InOrder inOrder = inOrder(executor, dummyFeature);
        inOrder.verify(executor).getVerifyDataAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getApplyCustomScriptAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getCleanupUsingScriptAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getCleanUpAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
    }

    @Test
    public void testExecuteAfterFailedTest() throws DbFeatureException {
        // GIVEN
        final Connection connection = new Connection();

        // WHEN
        executor.executeAfterTest(connection, true);

        // THEN
        final InOrder inOrder = inOrder(executor, dummyFeature);
        inOrder.verify(executor, never()).getVerifyDataAfterFeature();
        inOrder.verify(executor).getApplyCustomScriptAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getCleanupUsingScriptAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
        inOrder.verify(executor).getCleanUpAfterFeature();
        inOrder.verify(dummyFeature).execute(eq(connection));
    }
}
