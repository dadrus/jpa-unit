package eu.drus.jpa.unit.spi;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.api.CleanupStrategy;
import eu.drus.jpa.unit.api.DataSeedStrategy;
import eu.drus.jpa.unit.api.ExpectedDataSets;
import eu.drus.jpa.unit.spi.AbstractDbFeatureExecutor.NopFeature;

@RunWith(MockitoJUnitRunner.class)
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

    private DbFeatureExecutor executor;

    @Before
    public void setupMocks() {
        executor = spy(new DbFeatureExecutor(resolver));
    }

    @Test
    public void testCleanUpBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupBefore()).thenReturn(Boolean.FALSE);

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getCleanUpBeforeFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

        verify(executor, never()).createCleanupFeature(nullable(CleanupStrategy.class), nullable(List.class));
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
        verify(executor).createCleanupFeature(nullable(CleanupStrategy.class), nullable(List.class));
    }

    @Test
    public void testCleanUpAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupAfter()).thenReturn(Boolean.FALSE);

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getCleanUpAfterFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

        verify(executor, never()).createCleanupFeature(nullable(CleanupStrategy.class), nullable(List.class));
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
        verify(executor).createCleanupFeature(nullable(CleanupStrategy.class), nullable(List.class));
    }

    @Test
    public void testCleanupUsingScriptBeforeIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldCleanupUsingScriptBefore()).thenReturn(Boolean.FALSE);

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getCleanupUsingScriptBeforeFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

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

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getCleanupUsingScriptAfterFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

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

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getApplyCustomScriptBeforeFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

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

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getApplyCustomScriptAfterFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

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

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getSeedDataFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

        verify(executor, never()).createSeedDataFeature(nullable(DataSeedStrategy.class), nullable(List.class));
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
        verify(executor).createSeedDataFeature(nullable(DataSeedStrategy.class), nullable(List.class));
    }

    @Test
    public void testVerifyDataAfterIsDisabled() throws Exception {
        // GIVEN
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.FALSE);

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            final DbFeature<Connection> feature = executor.getVerifyDataAfterFeature();

            // THEN
            assertThat(feature, notNullValue());
            assertSame(feature, features.constructed().get(0));
        }

        verify(executor, never()).createVerifyDataAfterFeature(nullable(ExpectedDataSets.class));
    }

    @Test
    public void testVerifyDataAfterIsEnabled() throws Exception {
        // GIVEN
        when(resolver.shouldVerifyDataAfter()).thenReturn(Boolean.TRUE);

        // WHEN
        executor.getVerifyDataAfterFeature();

        // THEN
        verify(resolver).getExpectedDataSets();
        verify(executor).createVerifyDataAfterFeature(nullable(ExpectedDataSets.class));
    }

    @Test
    public void testExecuteBeforeTest() throws DbFeatureException {
        // GIVEN
        final Connection connection = new Connection();

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            executor.executeBeforeTest(connection);

            // THEN
            final List<NopFeature> constructed = features.constructed();
            final InOrder inOrder = inOrder(executor, constructed.get(0), constructed.get(1), constructed.get(2), constructed.get(3));
            inOrder.verify(executor).getCleanUpBeforeFeature();
            inOrder.verify(constructed.get(0)).execute(eq(connection));
            inOrder.verify(executor).getCleanupUsingScriptBeforeFeature();
            inOrder.verify(constructed.get(1)).execute(eq(connection));
            inOrder.verify(executor).getApplyCustomScriptBeforeFeature();
            inOrder.verify(constructed.get(2)).execute(eq(connection));
            inOrder.verify(executor).getSeedDataFeature();
            inOrder.verify(constructed.get(3)).execute(eq(connection));
        }
    }

    @Test
    public void testExecuteAfterSuccessfulTest() throws DbFeatureException {
        // GIVEN
        final Connection connection = new Connection();

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            executor.executeAfterTest(connection, false);

            // THEN
            final List<NopFeature> constructed = features.constructed();
            final InOrder inOrder = inOrder(executor, constructed.get(0), constructed.get(1), constructed.get(2), constructed.get(3));
            inOrder.verify(executor).getVerifyDataAfterFeature();
            inOrder.verify(constructed.get(0)).execute(eq(connection));
            inOrder.verify(executor).getApplyCustomScriptAfterFeature();
            inOrder.verify(constructed.get(1)).execute(eq(connection));
            inOrder.verify(executor).getCleanupUsingScriptAfterFeature();
            inOrder.verify(constructed.get(2)).execute(eq(connection));
            inOrder.verify(executor).getCleanUpAfterFeature();
            inOrder.verify(constructed.get(3)).execute(eq(connection));
        }
    }

    @Test
    public void testExecuteAfterFailedTest() throws DbFeatureException {
        // GIVEN
        final Connection connection = new Connection();

        try (MockedConstruction<NopFeature> features = mockConstruction(NopFeature.class)) {
            // WHEN
            executor.executeAfterTest(connection, true);

            // THEN
            final List<NopFeature> constructed = features.constructed();
            final InOrder inOrder = inOrder(executor, constructed.get(0), constructed.get(1), constructed.get(2));
            inOrder.verify(executor, never()).getVerifyDataAfterFeature();
            inOrder.verify(executor).getApplyCustomScriptAfterFeature();
            inOrder.verify(constructed.get(0)).execute(eq(connection));
            inOrder.verify(executor).getCleanupUsingScriptAfterFeature();
            inOrder.verify(constructed.get(1)).execute(eq(connection));
            inOrder.verify(executor).getCleanUpAfterFeature();
            inOrder.verify(constructed.get(2)).execute(eq(connection));
        }
    }
}
