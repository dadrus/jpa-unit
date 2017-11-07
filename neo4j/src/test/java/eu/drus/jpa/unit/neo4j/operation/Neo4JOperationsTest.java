package eu.drus.jpa.unit.neo4j.operation;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class Neo4JOperationsTest {

    @Test
    public void testCleanInsertIsOfProperType() {
        assertThat(Neo4JOperations.CLEAN_INSERT, instanceOf(CompositeOperation.class));
    }

    @Test
    public void testDeleteIsOfProperType() {
        assertThat(Neo4JOperations.DELETE, instanceOf(DeleteOperation.class));
    }

    @Test
    public void testDeleteAllIsOfProperType() {
        assertThat(Neo4JOperations.DELETE_ALL, instanceOf(DeleteAllOperation.class));
    }

    @Test
    public void testInsertIsOfProperType() {
        assertThat(Neo4JOperations.INSERT, instanceOf(InsertOperation.class));
    }

    @Test
    public void testRefreshIsOfProperType() {
        assertThat(Neo4JOperations.REFRESH, instanceOf(RefreshOperation.class));
    }

    @Test
    public void testUpdateIsOfProperType() {
        assertThat(Neo4JOperations.UPDATE, instanceOf(UpdateOperation.class));
    }
}
