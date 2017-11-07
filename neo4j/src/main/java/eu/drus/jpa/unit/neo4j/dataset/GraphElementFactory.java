package eu.drus.jpa.unit.neo4j.dataset;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.drus.jpa.unit.api.JpaUnitException;
import eu.drus.jpa.unit.neo4j.dataset.graphml.Attribute;
import eu.drus.jpa.unit.neo4j.dataset.graphml.EdgeProvider;
import eu.drus.jpa.unit.neo4j.dataset.graphml.VertexProvider;

public class GraphElementFactory implements VertexProvider<Node>, EdgeProvider<Node, Edge> {

    private static Map<List<String>, Class<?>> nodeLabelsEntityClassMap = new HashMap<>();

    private static Map<Class<?>, List<String>> entityClassIdPropertiesMap = new HashMap<>();

    private List<Class<?>> entityClasses;

    public GraphElementFactory(final List<Class<?>> entityClasses) {
        this.entityClasses = entityClasses;
    }

    private static Map<String, Object> convertAttributes(final Map<String, eu.drus.jpa.unit.neo4j.dataset.graphml.Attribute> attributes) {
        return attributes.entrySet().stream().filter(e -> !e.getKey().equals("labels") && !e.getKey().equals("label"))
                .collect(toMap(Entry::getKey, e -> AttributeTypeConverter.convert(e.getValue())));
    }

    private static List<String> extractLabels(final Map<String, eu.drus.jpa.unit.neo4j.dataset.graphml.Attribute> attributes) {
        return attributes.entrySet().stream().filter(e -> e.getKey().equals("labels") || e.getKey().equals("label"))
                .map(v -> v.getValue().getValue().split(":")).flatMap(Arrays::stream).filter(v -> !v.isEmpty()).sorted().collect(toList());
    }

    private List<eu.drus.jpa.unit.neo4j.dataset.Attribute> toNodeAttributes(final List<String> labels,
            final Map<String, Object> propertiesMap) throws NoSuchClassException {

        Class<?> nodeEntity = nodeLabelsEntityClassMap.get(labels);

        if (nodeEntity == null) {
            nodeEntity = EntityUtils.getEntityClassFromNodeLabels(labels, entityClasses);
            nodeLabelsEntityClassMap.put(labels, nodeEntity);
        }

        final List<String> entityIdList = entityClassIdPropertiesMap.computeIfAbsent(nodeEntity, EntityUtils::getNamesOfIdProperties);

        return propertiesMap.entrySet().stream()
                .map(e -> new eu.drus.jpa.unit.neo4j.dataset.Attribute(e.getKey(), e.getValue(), entityIdList.contains(e.getKey())))
                .collect(toList());
    }

    private List<eu.drus.jpa.unit.neo4j.dataset.Attribute> toEdgeAttributes(final Map<String, Object> propertiesMap) {
        return propertiesMap.entrySet().stream().map(e -> new eu.drus.jpa.unit.neo4j.dataset.Attribute(e.getKey(), e.getValue(), false))
                .collect(toList());
    }

    @Override
    public Edge buildEdge(final Node from, final Node to, final String name, final Map<String, Attribute> edgeAttributes) {
        return createEdge(from, to, name, extractLabels(edgeAttributes), convertAttributes(edgeAttributes));
    }

    public Edge createEdge(final Node from, final Node to, final String name, final List<String> labels,
            final Map<String, Object> attributesMap) {
        return new Edge(from, to, name, labels, toEdgeAttributes(attributesMap));
    }

    @Override
    public Node buildVertex(final String name, final Map<String, Attribute> nodeAttributes) {
        try {
            return createNode(name, extractLabels(nodeAttributes), convertAttributes(nodeAttributes));
        } catch (final NoSuchClassException e) {
            throw new JpaUnitException(e);
        }
    }

    public Node createNode(final String name, final List<String> labels, final Map<String, Object> attributesMap)
            throws NoSuchClassException {
        return new Node(name, labels, toNodeAttributes(labels, attributesMap));
    }
}
