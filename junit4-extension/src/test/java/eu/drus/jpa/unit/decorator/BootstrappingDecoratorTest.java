package eu.drus.jpa.unit.decorator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.lang.reflect.Method;
import java.util.Arrays;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.drus.jpa.unit.api.Bootstrapping;
import eu.drus.jpa.unit.core.metadata.AnnotationInspector;
import eu.drus.jpa.unit.core.metadata.MetadataExtractor;
import eu.drus.jpa.unit.spi.ExecutionContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BootstrappingDecorator.class)
public class BootstrappingDecoratorTest {

    @Mock
    private ExecutionContext ctx;

    @Mock
    private DataSource ds;

    @Mock
    private MetadataExtractor extractor;

    @Mock
    private AnnotationInspector<Bootstrapping> bootstrappingInpector;

    public void bootstrappingMethodOne(final DataSource ds) {}

    public void bootstrappingMethodTwo() {}

    @Before
    public void prepareMocks() throws Exception {
        whenNew(MetadataExtractor.class).withAnyArguments().thenReturn(extractor);

        when(ctx.getData("ds")).thenReturn(ds);
        when(extractor.bootstrapping()).thenReturn(bootstrappingInpector);
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
        decorator.beforeAll(ctx, this);

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
        decorator.beforeAll(ctx, this);

        // THEN
        // IllegalArgumentException is thrown. Only single method is allowed
    }

    @Test
    public void testBootstrappingMethodIsInvoked() throws Throwable {
        // GIVEN
        final Method bootstrappingMethodOne = getClass().getMethod("bootstrappingMethodOne", DataSource.class);
        when(bootstrappingInpector.getAnnotatedMethods()).thenReturn(Arrays.asList(bootstrappingMethodOne));
        final BootstrappingDecoratorTest thisSpy = spy(this);

        final BootstrappingDecorator decorator = new BootstrappingDecorator();

        // WHEN
        decorator.beforeAll(ctx, thisSpy);

        // THEN
        verify(thisSpy).bootstrappingMethodOne(eq(ds));
    }
}
