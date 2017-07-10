package eu.drus.jpa.unit.mongodb.operation;

public final class MongoDbOperations {
    private MongoDbOperations() {}

    public static final MongoDbOperation UPDATE = new UpdateOperation();

    public static final MongoDbOperation INSERT = new InsertOperation();

    public static final MongoDbOperation REFRESH = new RefreshOperation();

    public static final MongoDbOperation DELETE = new DeleteOperation();

    public static final MongoDbOperation DELETE_ALL = new DeleteAllOperation();

    public static final MongoDbOperation CLEAN_INSERT = new CompositeOperation(DELETE_ALL, INSERT);
}
