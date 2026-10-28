package eu.drus.jpa.unit.neo4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import eu.drus.jpa.unit.api.Bootstrapping;
import eu.drus.jpa.unit.core.metadata.AnnotationInspector;
import eu.drus.jpa.unit.core.metadata.MetadataExtractor;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestInvocation;

@RunWith(MockitoJUnitRunner.class)
public class BootstrappingDecoratorTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private TestInvocation invocation;

    @Mock
    private DataSource ds;

    @Mock
    private AnnotationInspector<Bootstrapping> bootstrappingInpector;

    private MockedConstruction<MetadataExtractor> extractorConstruction;

    public static void bootstrappingMethodOne(final DataSource ds) {}

    public static void bootstrappingMethodTwo() {}

    public void nonstaticMethod() {}

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Before
    public void prepareMocks() {
        extractorConstruction = mockConstruction(MetadataExtractor.class,
                (mock, context) -> lenient().when(mock.bootstrapping()).thenReturn(bootstrappingInpector));

        lenient().when(ctx.getData(eq(Constants.KEY_DATA_SOURCE))).thenReturn(ds);
        lenient().when(invocation.getContext()).thenReturn(ctx);
        lenient().when(invocation.getTestClass()).thenReturn((Class) getClass());
    }

    @After
    public void releaseMocks() {
        extractorConstruction.close();
    }

    @Test
    public void testRequiredPriority() {
        // GIVEN
        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        final int priority = decorator.getPriority();

        // THEN
        assertThat(priority, equalTo(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrappingMethodIsRequiredToHaveSingleParameterOfTypeDataSource() throws Throwable {
        // GIVEN
        final Method bootstrappingMethodTwo = getClass().getMethod("bootstrappingMethodTwo");
        when(bootstrappingInpector.getAnnotatedMethods()).thenReturn(Arrays.asList(bootstrappingMethodTwo));

        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        // IllegalArgumentException is thrown. A bootstrapping method is required to have a single
        // parameter of type DataSource
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultipleBootstrapingMethodsAreNotAllowed() throws Throwable {
        // GIVEN
        final Method bootstrappingMethodOne = getClass().getMethod("bootstrappingMethodOne", DataSource.class);
        final Method bootstrappingMethodTwo = getClass().getMethod("bootstrappingMethodTwo");
        when(bootstrappingInpector.getAnnotatedMethods()).thenReturn(Arrays.asList(bootstrappingMethodOne, bootstrappingMethodTwo));

        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        // IllegalArgumentException is thrown. Only single method is allowed
    }

    @Test
    public void testBootstrappingMethodIsInvoked() throws Throwable {
        // GIVEN
        final Method bootstrappingMethodOne = getClass().getMethod("bootstrappingMethodOne", DataSource.class);
        when(bootstrappingInpector.getAnnotatedMethods()).thenReturn(Arrays.asList(bootstrappingMethodOne));

        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        try (final MockedStatic<BootstrappingDecoratorTest> testClass = mockStatic(BootstrappingDecoratorTest.class)) {
            decorator.beforeAll(invocation);

            // THEN
            testClass.verify(() -> BootstrappingDecoratorTest.bootstrappingMethodOne(eq(ds)), times(1));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrappingMethodIsRequiredToBeStatic() throws Exception {
        // GIVEN
        final Method nonstaticMethod = getClass().getMethod("nonstaticMethod");
        when(bootstrappingInpector.getAnnotatedMethods()).thenReturn(Arrays.asList(nonstaticMethod));

        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        decorator.beforeAll(invocation);

        // THEN
        // IllegalArgumentException is thrown. Method must be static
    }

    public void testAfterAll() throws Exception {
        // GIVEN
        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        decorator.afterAll(invocation);

        // THEN
        verifyNoInteractions(invocation);
    }
}
