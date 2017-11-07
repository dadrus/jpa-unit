/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */

package eu.drus.jpa.unit.neo4j.dataset.graphml;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.ext.GraphImporter;
import org.jgrapht.ext.ImportException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GraphMLReader<V, E> implements GraphImporter<V, E> {

    private VertexProvider<V> vertexProvider;
    private EdgeProvider<V, E> edgeProvider;

    public GraphMLReader(final VertexProvider<V> vertexProvider, final EdgeProvider<V, E> edgeProvider) {
        checkArgument(vertexProvider != null, "Vertex provider cannot be null");
        checkArgument(edgeProvider != null, "Edge provider cannot be null");

        this.vertexProvider = vertexProvider;
        this.edgeProvider = edgeProvider;
    }

    @Override
    public void importGraph(final Graph<V, E> graph, final Reader in) throws ImportException {
        try {
            final SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            final SAXParser saxParser = spf.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            final GraphMLHandler<V, E> handler = new GraphMLHandler<>(graph, vertexProvider, edgeProvider);
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);
            xmlReader.parse(new InputSource(in));
        } catch (final Exception se) {
            throw new ImportException("Failed to parse GraphML", se);
        }
    }

    private static class GraphMLHandler<V, E> extends DefaultHandler {
        private static final String GRAPH = "graph";
        private static final String GRAPH_ID = "id";
        private static final String NODE = "node";
        private static final String NODE_ID = "id";
        private static final String EDGE = "edge";
        private static final String ALL = "all";
        private static final String EDGE_SOURCE = "source";
        private static final String EDGE_TARGET = "target";
        private static final String KEY = "key";
        private static final String KEY_FOR = "for";
        private static final String KEY_ATTR_NAME = "attr.name";
        private static final String KEY_ATTR_TYPE = "attr.type";
        private static final String KEY_ID = "id";
        private static final String DEFAULT = "default";
        private static final String DATA = "data";
        private static final String DATA_KEY = "key";

        private static final String EDGE_WEIGHT_ATTRIBUTE_NAME = "weight";

        // record state of parser
        private boolean insideDefault;
        private boolean insideData;

        // temporary state while reading elements
        // stack needed due to nested graphs in GraphML
        private Data currentData;
        private Key currentKey;
        private Deque<GraphElement> currentGraphElement;

        // collect custom keys
        private Map<String, Key> nodeValidKeys;
        private Map<String, Key> edgeValidKeys;
        private Map<String, V> graphNodes;
        private Graph<V, E> g;
        private VertexProvider<V> vertexProvider;
        private EdgeProvider<V, E> edgeProvider;

        public GraphMLHandler(final Graph<V, E> graph, final VertexProvider<V> vertexProvider, final EdgeProvider<V, E> edgeProvider) {
            this.g = graph;
            this.vertexProvider = vertexProvider;
            this.edgeProvider = edgeProvider;
        }

        @Override
        public void startDocument() throws SAXException {
            nodeValidKeys = new HashMap<>();
            edgeValidKeys = new HashMap<>();
            graphNodes = new HashMap<>();
            insideDefault = false;
            insideData = false;
            currentKey = null;
            currentData = null;
            currentGraphElement = new ArrayDeque<>();
            currentGraphElement.push(new GraphElement("graphml"));
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
                throws SAXException {
            switch (localName) {
            case GRAPH:
                currentGraphElement.push(new GraphElement(findAttribute(GRAPH_ID, attributes)));
                break;
            case NODE:
                currentGraphElement.push(new GraphElement(findAttribute(NODE_ID, attributes)));
                break;
            case EDGE:
                currentGraphElement.push(new GraphElement(findAttribute(EDGE_SOURCE, attributes), findAttribute(EDGE_TARGET, attributes)));
                break;
            case KEY:
                final String keyId = findAttribute(KEY_ID, attributes);
                final String keyAttrName = findAttribute(KEY_ATTR_NAME, attributes);
                final String keyAttrType = findAttribute(KEY_ATTR_TYPE, attributes);
                final String keyFor = findAttribute(KEY_FOR, attributes);
                currentKey = new Key(keyId, keyAttrName, AttributeType.create(keyAttrType), toKeyTarget(keyFor));
                break;
            case DEFAULT:
                insideDefault = true;
                break;
            case DATA:
                insideData = true;
                currentData = new Data(findAttribute(DATA_KEY, attributes), null);
                break;
            default:
                break;
            }
        }

        private KeyTarget toKeyTarget(final String keyFor) {
            if (NODE.equals(keyFor)) {
                return KeyTarget.NODE;
            } else if (EDGE.equals(keyFor)) {
                return KeyTarget.EDGE;
            } else if (ALL.equals(keyFor)) {
                return KeyTarget.ALL;
            }
            return null;
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            switch (localName) {
            case GRAPH:
                currentGraphElement.pop();
                break;
            case NODE:
                addVertext(currentGraphElement.pop());
                break;
            case EDGE:
                addEdge(currentGraphElement.pop());
                break;
            case KEY:
                if (currentKey.isValid()) {
                    switch (currentKey.target) {
                    case NODE:
                        nodeValidKeys.put(currentKey.id, currentKey);
                        break;
                    case EDGE:
                        edgeValidKeys.put(currentKey.id, currentKey);
                        break;
                    case ALL:
                        nodeValidKeys.put(currentKey.id, currentKey);
                        edgeValidKeys.put(currentKey.id, currentKey);
                        break;
                    }
                }
                currentKey = null;
                break;
            case DEFAULT:
                insideDefault = false;
                break;
            case DATA:
                if (currentData.isValid()) {
                    currentGraphElement.peek().attributes.put(currentData.key, currentData.value);
                }
                insideData = false;
                currentData = null;
                break;
            default:
                break;
            }
        }

        private void addVertext(final GraphElement currentNode) throws SAXException {
            verifyCondition(!graphNodes.containsKey(currentNode.id1), "Node with id " + currentNode.id1 + " already exists");

            final V v = vertexProvider.buildVertex(currentNode.id1, getAttributes(currentNode, nodeValidKeys));

            graphNodes.put(currentNode.id1, v);
            g.addVertex(v);
        }

        private void addEdge(final GraphElement p) throws SAXException {
            // create edges
            final V from = graphNodes.get(p.id1);
            verifyCondition(from != null, "Source vertex " + p.id1 + " not found");

            final V to = graphNodes.get(p.id2);
            verifyCondition(to != null, "Target vertex " + p.id2 + " not found");

            // create attributes
            final Map<String, Attribute> attributes = getAttributes(p, edgeValidKeys);

            final E e = edgeProvider.buildEdge(from, to, "e_" + from + "_" + to, attributes);

            setEdgeWeightIfRequired(e, attributes);

            g.addEdge(from, to, e);
        }

        private void setEdgeWeightIfRequired(final E e, final Map<String, Attribute> attributes) {
            // special handling for weighted graphs
            if (g instanceof WeightedGraph<?, ?> && attributes.containsKey(EDGE_WEIGHT_ATTRIBUTE_NAME)) {
                try {
                    ((WeightedGraph<V, E>) g).setEdgeWeight(e, Float.valueOf(attributes.get(EDGE_WEIGHT_ATTRIBUTE_NAME).getValue()));
                } catch (final NumberFormatException nfe) {
                    ((WeightedGraph<V, E>) g).setEdgeWeight(e, getEdgeWeight());
                }
            }
        }

        private double getEdgeWeight() {
            for (final Key k : edgeValidKeys.values()) {
                if (k.attributeName.equals(EDGE_WEIGHT_ATTRIBUTE_NAME)) {
                    try {
                        if (k.defaultValue != null) {
                            return Double.parseDouble(k.defaultValue);
                        }
                    } catch (final NumberFormatException e) {
                        return WeightedGraph.DEFAULT_EDGE_WEIGHT;
                    }
                }
            }

            return WeightedGraph.DEFAULT_EDGE_WEIGHT;
        }

        private Map<String, Attribute> getAttributes(final GraphElement ge, final Map<String, Key> keyReferences) {
            final Map<String, Attribute> attributes = new HashMap<>();

            for (final Key keyReference : keyReferences.values()) {
                if (ge.attributes.containsKey(keyReference.id)) {
                    final Attribute attribute = new DefaultAttribute<>(ge.attributes.get(keyReference.id), keyReference.attributeType);
                    attributes.put(keyReference.attributeName, attribute);
                } else if (keyReference.defaultValue != null) {
                    final Attribute attribute = new DefaultAttribute<>(keyReference.defaultValue, keyReference.attributeType);
                    attributes.put(keyReference.attributeName, attribute);
                }
            }

            return attributes;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (insideDefault) {
                currentKey.defaultValue = new String(ch, start, length);
            } else if (insideData) {
                currentData.value = new String(ch, start, length);
            }
        }

        @Override
        public void warning(final SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void error(final SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(final SAXParseException e) throws SAXException {
            throw e;
        }

        private String findAttribute(final String localName, final Attributes attributes) {
            for (int i = 0; i < attributes.getLength(); i++) {
                final String attrLocalName = attributes.getLocalName(i);
                if (attrLocalName.equals(localName)) {
                    return attributes.getValue(i);
                }
            }
            return null;
        }

        private static void verifyCondition(final boolean expression, final String message) throws SAXException {
            if (!expression) {
                throw new SAXException(message);
            }
        }

    }

    // ----- Helper classes for storing partial parser results -----

    private enum KeyTarget {
        NODE,
        EDGE,
        ALL;
    }

    private static class Key {
        String id;
        AttributeType attributeType;
        String attributeName;
        String defaultValue;
        KeyTarget target;

        public Key(final String id, final String attributeName, final AttributeType attributeType, final KeyTarget target) {
            this.id = id;
            this.attributeName = attributeName;
            this.attributeType = attributeType;
            this.target = target;
        }

        public boolean isValid() {
            return id != null && attributeName != null && target != null && attributeType != null;
        }

    }

    private static class Data {
        String key;
        String value;

        public Data(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public boolean isValid() {
            return key != null && value != null;
        }
    }

    private static class GraphElement {
        String id1;
        String id2;
        Map<String, String> attributes;

        public GraphElement(final String id1) {
            this(id1, null);
        }

        public GraphElement(final String id1, final String id2) {
            this.id1 = id1;
            this.id2 = id2;
            attributes = new HashMap<>();
        }
    }

}
