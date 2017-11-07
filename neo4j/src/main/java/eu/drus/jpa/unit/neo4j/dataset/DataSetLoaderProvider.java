package eu.drus.jpa.unit.neo4j.dataset;

import java.io.File;
import java.io.IOException;

import org.jgrapht.Graph;
import org.jgrapht.ext.ImportException;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.DefaultDirectedGraph;

import eu.drus.jpa.unit.spi.DataSetFormat.LoaderProvider;
import eu.drus.jpa.unit.neo4j.dataset.graphml.GraphMLReader;
import eu.drus.jpa.unit.spi.DataSetLoader;
import eu.drus.jpa.unit.spi.UnsupportedDataSetFormatException;

public class DataSetLoaderProvider implements LoaderProvider<Graph<Node, Edge>> {

    private GraphElementFactory graphElementFactory;

    public DataSetLoaderProvider(final GraphElementFactory graphElementFactory) {
        this.graphElementFactory = graphElementFactory;
    }

    @Override
    public DataSetLoader<Graph<Node, Edge>> xmlLoader() {
        return (final File path) -> {
            try {
                final DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<>(new ClassBasedEdgeFactory<>(Edge.class));
                final GraphMLReader<Node, Edge> importer = new GraphMLReader<>(graphElementFactory, graphElementFactory);
                importer.importGraph(graph, path);
                return graph;
            } catch (final ImportException e) {
                throw new IOException(e);
            }
        };

    }

    @Override
    public DataSetLoader<Graph<Node, Edge>> yamlLoader() {
        throw new UnsupportedDataSetFormatException("YAML data sets are not supportred for Neo4j");
    }

    @Override
    public DataSetLoader<Graph<Node, Edge>> jsonLoader() {
        throw new UnsupportedDataSetFormatException("JSON data sets are not supportred for Neo4j");
    }

    @Override
    public DataSetLoader<Graph<Node, Edge>> csvLoader() {
        throw new UnsupportedDataSetFormatException("CSV data sets are not supportred for Neo4j");
    }

    @Override
    public DataSetLoader<Graph<Node, Edge>> xlsLoader() {
        throw new UnsupportedDataSetFormatException("XSL data sets are not supportred for Neo4j");
    }

}
