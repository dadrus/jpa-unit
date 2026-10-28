package eu.drus.jpa.unit.neo4j.dataset;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.neo4j.cypherdsl.core.Cypher;
import org.neo4j.cypherdsl.core.Relationship;

public class Edge extends GraphElement {

    private Node from;
    private Node to;

    Edge(final Node from, final Node to, final String id, final List<String> labels, final List<Attribute> attributes) {
        super(id, labels, attributes);
        this.from = from;
        this.to = to;
    }

    public Node getSourceNode() {
        return from;
    }

    public Node getTargetNode() {
        return to;
    }

    public PathBuilder toPath() {
        return new PathBuilder();
    }

    @Override
    public String toString() {
        return getId();
    }

    public boolean isSame(final Edge other, final List<String> attributesToExclude) {
        if (!super.isSame(other, attributesToExclude)) {
            return false;
        }

        return from.isSame(other.from, attributesToExclude) && to.isSame(other.to, attributesToExclude);
    }

    public String asString() {
        final ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.append("id", getId());
        builder.append("labels", getLabels());
        builder.append("from", from);
        builder.append("to", to);
        builder.append("attributes", getAttributes());
        return builder.build();
    }

    public class PathBuilder {

        private boolean includeAllAttributes = false;
        private boolean includeNodeIdAttributes = false;

        private PathBuilder() {}

        public PathBuilder withAllAttributes() {
            includeAllAttributes = true;
            return this;
        }

        public PathBuilder withNodeIdAttributes() {
            includeNodeIdAttributes = true;
            return this;
        }

        public Relationship build() {
            final org.neo4j.cypherdsl.core.Node source = includeNodeIdAttributes
                    ? from.nodePattern(from.getId(), from.idAttributes())
                    : Cypher.anyNode(from.getId());
            final org.neo4j.cypherdsl.core.Node target = includeNodeIdAttributes
                    ? to.nodePattern(to.getId(), to.idAttributes())
                    : Cypher.anyNode(to.getId());

            final Relationship relationship = source.relationshipTo(target, getLabels().toArray(new String[0])).named(getId());

            return includeAllAttributes ? relationship.withProperties(mapExpression(getAttributes())) : relationship;
        }
    }
}
