package eu.drus.jpa.unit.mongodb.operation;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

public class CompositeOperation implements MongoDbOperation {

    private MongoDbOperation[] operations;

    public CompositeOperation(final MongoDbOperation... operations) {
        this.operations = operations;
    }

    @Override
    public void execute(final MongoDatabase connection, final Document data) {

        for (final MongoDbOperation operation : operations) {
            operation.execute(connection, data);
        }
    }

}
