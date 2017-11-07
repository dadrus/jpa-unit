package eu.drus.jpa.unit.neo4j;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import javax.sql.DataSource;

import eu.drus.jpa.unit.core.metadata.MetadataExtractor;
import eu.drus.jpa.unit.neo4j.ext.ConfigurationRegistry;
import eu.drus.jpa.unit.spi.ExecutionContext;
import eu.drus.jpa.unit.spi.TestClassDecorator;
import eu.drus.jpa.unit.spi.TestInvocation;

public class BootstrappingDecorator implements TestClassDecorator {

    private ConfigurationRegistry configurationRegistry = new ConfigurationRegistry();

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void beforeAll(final TestInvocation invocation) throws Exception {

        final DataSource ds = (DataSource) invocation.getContext().getData(Constants.KEY_DATA_SOURCE);

        final MetadataExtractor extractor = new MetadataExtractor(invocation.getTestClass());
        final List<Method> bootstrappingMethods = extractor.bootstrapping().getAnnotatedMethods();
        checkArgument(bootstrappingMethods.size() <= 1, "Only single method is allowed to be annotated with @Bootstrapping");

        if (!bootstrappingMethods.isEmpty()) {
            final Method tmp = bootstrappingMethods.get(0);
            checkArgument(Modifier.isStatic(tmp.getModifiers()), "A bootstrapping method is required to be static");

            final Class<?>[] parameterTypes = tmp.getParameterTypes();
            checkArgument(parameterTypes.length == 1, "A bootstrapping method is required to have a single parameter of type DataSource");
            checkArgument(parameterTypes[0].equals(DataSource.class),
                    "A bootstrapping method is required to have a single parameter of type DataSource");

            tmp.invoke(null, ds);
        }
    }

    @Override
    public void afterAll(final TestInvocation invocation) throws Exception {
        // nothing to do here
    }

    @Override
    public boolean isConfigurationSupported(final ExecutionContext ctx) {
        return configurationRegistry.hasConfiguration(ctx.getDescriptor());
    }

}
