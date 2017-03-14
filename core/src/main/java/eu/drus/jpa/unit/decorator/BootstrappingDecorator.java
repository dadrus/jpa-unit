package eu.drus.jpa.unit.decorator;

import static eu.drus.jpa.unit.util.Preconditions.checkArgument;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.sql.DataSource;

import eu.drus.jpa.unit.core.metadata.MetadataExtractor;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;

public class BootstrappingDecorator implements TestClassDecorator {

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        final MetadataExtractor extractor = new MetadataExtractor(testClass);
        final List<Method> bootstrappingMethods = extractor.bootstrapping().getAnnotatedMethods();
        checkArgument(bootstrappingMethods.size() <= 1, "Only single method is allowed to be annotated with @Bootstrapping");

        if (!bootstrappingMethods.isEmpty()) {
            final Method tmp = bootstrappingMethods.get(0);
            checkArgument(Modifier.isStatic(tmp.getModifiers()), "A bootstrapping method is required to be static");

            final Class<?>[] parameterTypes = tmp.getParameterTypes();
            checkArgument(parameterTypes.length == 1, "A bootstrapping method is required to have a single parameter of type DataSource");
            checkArgument(parameterTypes[0].equals(DataSource.class),
                    "A bootstrapping method is required to have a single parameter of type DataSource");

            tmp.invoke(null, (DataSource) ctx.getData("ds"));
        }
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Class<?> testClass) throws Exception {
        // nothing to do here
    }

}
