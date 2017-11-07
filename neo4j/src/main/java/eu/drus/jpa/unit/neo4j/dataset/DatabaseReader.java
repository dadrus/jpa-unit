package eu.drus.jpa.unit.neo4j.dataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

public class DatabaseReader {

    private GraphElementFactory factory;

    public DatabaseReader(final GraphElementFactory factory) {
        this.factory = factory;
    }

    public Graph<Node, Edge> readGraph(final Connection connection) throws SQLException {
        final List<Node> nodes = new ArrayList<>();
        final List<Edge> edges = new ArrayList<>();

        readGraphElements(connection, edges, nodes);

        final DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));
        nodes.forEach(graph::addVertex);
        edges.forEach(e -> graph.addEdge(e.getSourceNode(), e.getTargetNode(), e));

        return graph;
    }

    @SuppressWarnings("unchecked")
    private void readGraphElements(final Connection connection, final List<Edge> edgeList, final List<Node> nodeList) throws SQLException {
        final Map<Object, Node> nodes = new HashMap<>();

        try (PreparedStatement ps = connection
                .prepareStatement("MATCH (n) RETURN { id: id(n), labels: labels(n), attributes: properties(n) } as node")) {
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        final Map<String, ?> n = (Map<String, ?>) rs.getObject("node");
                        final Node node = toNode(n);
                        nodes.put(n.get("id"), node);
                        nodeList.add(node);
                    } catch (final NoSuchClassException e1) {
                        // happens, when the db contains nodes which are not part of the given
                        // domain model. E.g. like special objects from the persistence provider.
                        // Such nodes have to be ignored
                    }
                }
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "MATCH ()-[r]->() RETURN {id: id(r), label: type(r), attributes: properties(r), from: id(startNode(r)), to: id(endNode(r))} as relation")) {
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Map<String, ?> r = (Map<String, ?>) rs.getObject("relation");

                    final Edge edge = toEdge(nodes, r);
                    if (edge != null) {
                        edgeList.add(edge);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Node toNode(final Map<String, ?> node) throws NoSuchClassException {
        final String id = node.get("id").toString();
        final List<String> labels = (List<String>) node.get("labels");
        final Map<String, Object> attributes = (Map<String, Object>) node.get("attributes");

        return factory.createNode(id, labels, attributes);
    }

    @SuppressWarnings("unchecked")
    private Edge toEdge(final Map<Object, Node> nodes, final Map<String, ?> edge) {
        final String id = edge.get("id").toString();
        final List<String> labels = Arrays.asList(edge.get("label").toString());
        final Map<String, Object> attributes = (Map<String, Object>) edge.get("attributes");

        final Node from = nodes.get(edge.get("from"));
        final Node to = nodes.get(edge.get("to"));

        if (to == null || from == null) {
            // happens, when the edge is between nodes, which are not part of the given domain model
            return null;
        }

        return factory.createEdge(from, to, id, labels, attributes);
    }
}
