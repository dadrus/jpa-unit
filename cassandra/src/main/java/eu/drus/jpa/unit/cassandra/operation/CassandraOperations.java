package eu.drus.jpa.unit.cassandra.operation;

public final class CassandraOperations {

    private CassandraOperations() {}

    public static final CassandraOperation DELETE_ALL = new DeleteAllOperation();

    public static final CassandraOperation DELETE = new DeleteOperation();

    public static final CassandraOperation INSERT = new InsertOperation();

    public static final CassandraOperation REFRESH = new RefreshOperation();

    public static final CassandraOperation UPDATE = new UpdateOperation();

    public static final CassandraOperation CLEAN_INSERT = new CompositeOperation(DELETE_ALL, INSERT);

}
