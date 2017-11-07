package eu.drus.jpa.unit.neo4j.dataset;

import static java.util.stream.Collectors.toList;
import static org.neo4j.cypherdsl.CypherQuery.node;
import static org.neo4j.cypherdsl.CypherQuery.value;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.neo4j.cypherdsl.CypherQuery;
import org.neo4j.cypherdsl.Identifier;
import org.neo4j.cypherdsl.Path;
import org.neo4j.cypherdsl.PathRelationship;

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

        public Path build() {
            final List<Identifier> relationShips = getLabels().stream().map(CypherQuery::identifier).collect(toList());
            final Path fromPath = includeNodeIdAttributes ? from.toPath().withIdAttributes().build() : node(from.getId());
            final Path toPath = includeNodeIdAttributes ? to.toPath().withIdAttributes().build() : node(to.getId());
            PathRelationship path = fromPath.out(relationShips.toArray(new Identifier[relationShips.size()])).as(getId());
            if (includeAllAttributes) {
                path = path.values(getAttributes().stream().map(a -> value(a.getName(), a.getValue())).collect(toList()));
            }

            final String nodeAsString = toPath.toString();
            // need to remove () around the expression. they are added again by the node() call
            return path.node(nodeAsString.substring(1, nodeAsString.length() - 1));
        }
    }
}
