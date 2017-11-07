package eu.drus.jpa.unit.neo4j.dataset;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.jgrapht.Graph;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.spi.AssertionErrorCollector;
import eu.drus.jpa.unit.spi.ColumnsHolder;

public class GraphComparator {

    private static final String FAILED_TO_VERIFY_DATA_BASE_STATE = "Failed to verify data base state";

    private static final Function<String, String> ID_MAPPER = (final String name) -> name.equalsIgnoreCase("ID") ? "id" : name;

    private ColumnsHolder toExclude;
    private boolean isStrict;
    private DatabaseReader dbReader;

    public GraphComparator(final GraphElementFactory factory, final String[] toExclude, final boolean strict) {
        this.toExclude = new ColumnsHolder(toExclude, ID_MAPPER);
        isStrict = strict;
        dbReader = new DatabaseReader(factory);
    }

    public void compare(final Connection connection, final Graph<Node, Edge> expectedGraph, final AssertionErrorCollector errorCollector) {
        Graph<Node, Edge> givenGraph;
        try {
            givenGraph = dbReader.readGraph(connection);
        } catch (final SQLException e) {
            throw new JpaUnitException(FAILED_TO_VERIFY_DATA_BASE_STATE, e);
        }

        if (expectedGraph.vertexSet().isEmpty()) {
            shouldBeEmpty(givenGraph, errorCollector);
        } else {
            compareContent(givenGraph, expectedGraph, errorCollector);
        }
    }

    private void shouldBeEmpty(final Graph<Node, Edge> givenGraph, final AssertionErrorCollector errorCollector) {
        final Map<String, Integer> unexpectedNodesOccurence = new HashMap<>();

        for (final Node node : givenGraph.vertexSet()) {
            unexpectedNodesOccurence.compute(node.getType(), (k, v) -> v == null ? 1 : v + 1);
        }

        for (final Entry<String, Integer> nodeEntries : unexpectedNodesOccurence.entrySet()) {
            if (nodeEntries.getValue() != 0) {
                errorCollector.collect("No nodes with " + nodeEntries.getKey() + " labels were expected, but there are <"
                        + nodeEntries.getValue() + "> nodes present.");
            }
        }
    }

    private void compareContent(final Graph<Node, Edge> givenGraph, final Graph<Node, Edge> expectedGraph,
            final AssertionErrorCollector errorCollector) {

        final Set<String> expectedNodeTypes = expectedGraph.vertexSet().stream().map(Node::getType).collect(toSet());

        final Set<String> currentNodeTypes = givenGraph.vertexSet().stream().map(Node::getType).collect(toSet());

        verifyNodeLabels(currentNodeTypes, expectedNodeTypes, errorCollector,
                "Nodes with %s labels were expected to be present, but not found");

        checkPresenceOfExpectedNodes(givenGraph, expectedGraph, errorCollector);
        checkPresenceOfExpectedReferences(givenGraph, expectedGraph, errorCollector);
        checkAbsenseOfNotExpectedNodes(givenGraph, expectedGraph, errorCollector);
        checkAbsenseOfNotExpectedReferences(givenGraph, expectedGraph, errorCollector);

        if (isStrict) {
            verifyNodeLabels(expectedNodeTypes, currentNodeTypes, errorCollector,
                    "Nodes with %s labels were not expected, but are present");
        }
    }

    private void verifyNodeLabels(final Collection<String> currentNodeLabels, final Collection<String> expectedNodeLabels,
            final AssertionErrorCollector errorCollector, final String formatString) {
        for (final String expectedNodeLabel : expectedNodeLabels) {
            if (!currentNodeLabels.contains(expectedNodeLabel)) {
                errorCollector.collect(String.format(formatString, expectedNodeLabel));
            }
        }
    }

    private void checkPresenceOfExpectedNodes(final Graph<Node, Edge> givenGraph, final Graph<Node, Edge> expectedGraph,
            final AssertionErrorCollector errorCollector) {
        for (final Node expectedNode : expectedGraph.vertexSet()) {

            final List<String> attributesToExclude = expectedNode.getLabels().stream().map(toExclude::getColumns).flatMap(List::stream)
                    .distinct().collect(toList());

            final List<Node> availableNodesOfExpectedType = givenGraph.vertexSet().stream()
                    .filter(n -> n.getType().equals(expectedNode.getType())).collect(toList());

            final List<Node> foundNodes = availableNodesOfExpectedType.stream().filter(n -> n.isSame(expectedNode, attributesToExclude))
                    .collect(toList());

            if (foundNodes.isEmpty()) {
                errorCollector.collect(expectedNode.asString() + " was expected, but is not present");
            } else if (foundNodes.size() > 1) {
                errorCollector.collect("Ambiguouty detected for node " + expectedNode.asString() + " for given attribute filter");
            }
        }
    }

    private void checkPresenceOfExpectedReferences(final Graph<Node, Edge> givenGraph, final Graph<Node, Edge> expectedGraph,
            final AssertionErrorCollector errorCollector) {
        for (final Edge expectedEdge : expectedGraph.edgeSet()) {

            final List<String> attributesToExclude = expectedEdge.getLabels().stream().map(toExclude::getColumns).flatMap(List::stream)
                    .distinct().collect(toList());

            final List<Edge> availableEdgesOfExpectedType = givenGraph.edgeSet().stream()
                    .filter(e -> e.getType().equals(expectedEdge.getType())).collect(toList());

            final List<Edge> foundEdges = availableEdgesOfExpectedType.stream().filter(e -> e.isSame(expectedEdge, attributesToExclude))
                    .collect(toList());

            if (foundEdges.isEmpty()) {
                errorCollector.collect(expectedEdge.asString() + " was expected, but is not present");
            } else if (foundEdges.size() > 1) {
                errorCollector.collect("Ambiguouty detected for edge " + expectedEdge.asString() + " for given attribute filter");
            }
        }
    }

    private void checkAbsenseOfNotExpectedNodes(final Graph<Node, Edge> givenGraph, final Graph<Node, Edge> expectedGraph,
            final AssertionErrorCollector errorCollector) {

        final List<List<String>> expectedNodeLables = expectedGraph.vertexSet().stream().map(Node::getLabels).distinct().collect(toList());

        for (final List<String> labels : expectedNodeLables) {
            final List<Node> expectedNodes = expectedGraph.vertexSet().stream().filter(node -> node.getLabels().containsAll(labels))
                    .collect(toList());

            final List<String> attributesToExclude = labels.stream().map(toExclude::getColumns).flatMap(List::stream).distinct()
                    .collect(toList());

            for (final Node givenNode : givenGraph.vertexSet()) {
                if (!givenNode.getLabels().containsAll(labels)) {
                    continue;
                }

                final boolean nodePresent = expectedNodes.stream().anyMatch(node -> {
                    final Set<Attribute> attributesToRetain = node.getAttributes().stream()
                            .filter(a -> !attributesToExclude.contains(a.getName())).collect(toSet());

                    return givenNode.getAttributes().containsAll(attributesToRetain);
                });

                if (!nodePresent) {
                    errorCollector.collect(givenNode.asString() + " was not expected, but is present");
                }
            }
        }
    }

    private void checkAbsenseOfNotExpectedReferences(final Graph<Node, Edge> givenGraph, final Graph<Node, Edge> expectedGraph,
            final AssertionErrorCollector errorCollector) {
        final List<List<String>> expectedEdgeLables = expectedGraph.edgeSet().stream().map(Edge::getLabels).distinct().collect(toList());

        for (final List<String> labels : expectedEdgeLables) {
            final List<Edge> expectedEdges = expectedGraph.edgeSet().stream().filter(node -> node.getLabels().containsAll(labels))
                    .collect(toList());

            final List<String> edgeAttributesToExclude = labels.stream().map(toExclude::getColumns).flatMap(List::stream).distinct()
                    .collect(toList());

            for (final Edge givenEdge : givenGraph.edgeSet()) {
                if (!givenEdge.getLabels().containsAll(labels)) {
                    continue;
                }

                final boolean edgePresent = expectedEdges.stream().anyMatch(edge -> {
                    final Set<Attribute> attributesToRetain = edge.getAttributes().stream()
                            .filter(a -> !edgeAttributesToExclude.contains(a.getName())).collect(toSet());

                    final List<String> sourceNodeAttributesToExclude = edge.getSourceNode().getLabels().stream().map(toExclude::getColumns)
                            .flatMap(List::stream).distinct().collect(toList());

                    final List<String> targetNodeAttributesToExclude = edge.getTargetNode().getLabels().stream().map(toExclude::getColumns)
                            .flatMap(List::stream).distinct().collect(toList());

                    return givenEdge.getAttributes().containsAll(attributesToRetain)
                            && givenEdge.getSourceNode().isSame(edge.getSourceNode(), sourceNodeAttributesToExclude)
                            && givenEdge.getTargetNode().isSame(edge.getTargetNode(), targetNodeAttributesToExclude);
                });

                if (!edgePresent) {
                    errorCollector.collect(givenEdge.asString() + " was not expected, but is present");
                }
            }
        }
    }
}
