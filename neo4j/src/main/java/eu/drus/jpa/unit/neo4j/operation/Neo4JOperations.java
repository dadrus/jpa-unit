package eu.drus.jpa.unit.neo4j.operation;

public final class Neo4JOperations {

    private Neo4JOperations() {}

    public static final Neo4JOperation UPDATE = new UpdateOperation();

    public static final Neo4JOperation INSERT = new InsertOperation();

    public static final Neo4JOperation REFRESH = new RefreshOperation();

    public static final Neo4JOperation DELETE = new DeleteOperation();

    public static final Neo4JOperation DELETE_ALL = new DeleteAllOperation();

    public static final Neo4JOperation CLEAN_INSERT = new CompositeOperation(DELETE_ALL, INSERT);
}
