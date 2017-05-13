package eu.drus.jpa.unit.decorator.mongodb;

import org.bson.conversions.Bson;

import com.mongodb.client.MongoDatabase;

public abstract class AbstractDbOperation {

    public static final AbstractDbOperation UPDATE = new UpdateOperation();

    public static final AbstractDbOperation INSERT = new InsertOperation();

    public static final AbstractDbOperation REFRESH = new RefreshOperation();

    public static final AbstractDbOperation DELETE = new DeleteOperation();

    public static final AbstractDbOperation DELETE_ALL = new DeleteAllOperation();

    public static final AbstractDbOperation CLEAN_INSERT = new CompositeDbOperation(DELETE_ALL, INSERT);

    public abstract void execute(MongoDatabase connection, Bson data);
}
