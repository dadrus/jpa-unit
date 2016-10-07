package eu.drus.test.persistence.core.dbunit.dataset;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.DataSetException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

public class YamlDataSetProducer extends DataSetProducer {

    public YamlDataSetProducer(final InputStream input) {
        super(input);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Map<String, List<Map<String, String>>> loadDataSet() throws DataSetException {
        try {
            final Object yamlData = createYamlReader().load(input);
            if (yamlData == null) {
                return Collections.emptyMap();
            }

            return (Map<String, List<Map<String, String>>>) yamlData;
        } catch (final Exception e) {
            throw new DataSetException("Error parsing yaml data set", e);
        }
    }

    public Yaml createYamlReader() {
        final Yaml yaml = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new Resolver() {
            @Override
            protected void addImplicitResolvers() {
                // Intentionally left TIMESTAMP as string to let DBUnit deal with the conversion
                addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
                addImplicitResolver(Tag.INT, INT, "-+0123456789");
                addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
                addImplicitResolver(Tag.MERGE, MERGE, "<");
                addImplicitResolver(Tag.NULL, NULL, "~nN\0");
                addImplicitResolver(Tag.NULL, EMPTY, null);
                addImplicitResolver(Tag.VALUE, VALUE, "=");
                addImplicitResolver(Tag.YAML, YAML, "!&*");
            }
        });
        return yaml;
    }
}
