package eu.drus.jpa.unit.neo4j.operation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class AbstractNeo4JOperation implements Neo4JOperation {

    protected void executeQuery(final Connection connection, final String query) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) { /* nothing to do */ }
        }
    }
}
