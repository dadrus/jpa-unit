package eu.drus.jpa.unit.neo4j.dataset;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.neo4j.cypherdsl.core.Cypher;

public class Node extends GraphElement {

    Node(final String id, final List<String> labels, final List<Attribute> attributes) {
        super(id, labels, attributes);
    }

    public PathBuilder toPath() {
        return new PathBuilder();
    }

    @Override
    public String toString() {
        return getId();
    }

    public String asString() {
        final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("id", getId());
        builder.append("labels", getLabels());
        builder.append("attributes", getAttributes());
        return builder.build();
    }

    org.neo4j.cypherdsl.core.Node nodePattern(final String name, final List<Attribute> properties) {
        final List<String> labels = getLabels();
        final org.neo4j.cypherdsl.core.Node pattern = labels.isEmpty()
                ? Cypher.anyNode(name)
                : Cypher.node(labels.get(0), labels.subList(1, labels.size()).toArray(new String[0])).named(name);

        return properties.isEmpty() ? pattern : pattern.withProperties(mapExpression(properties));
    }

    List<Attribute> idAttributes() {
        return getAttributes().stream().filter(Attribute::isId).collect(toList());
    }

    public class PathBuilder {

        private String name = getId();

        private List<Attribute> properties = Collections.emptyList();

        private PathBuilder() {
        }

        public PathBuilder withId(final String id) {
            name = id;
            return this;
        }

        public PathBuilder withAllAttributes() {
            properties = getAttributes();
            return this;
        }

        public PathBuilder withIdAttributes() {
            properties = idAttributes();
            return this;
        }

        public org.neo4j.cypherdsl.core.Node build() {
            return nodePattern(name, properties);
        }
    }
}
