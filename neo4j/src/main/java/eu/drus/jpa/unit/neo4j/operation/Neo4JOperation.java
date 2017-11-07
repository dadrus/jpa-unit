package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.SQLException;

import org.jgrapht.Graph;

import eu.drus.jpa.unit.neo4j.dataset.Edge;
import eu.drus.jpa.unit.neo4j.dataset.Node;

public interface Neo4JOperation {
    void execute(Connection connection, Graph<Node, Edge> graph) throws SQLException;
}
