package eu.drus.jpa.unit.mongodb;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

public class CompositeDbOperation extends AbstractDbOperation {

    private AbstractDbOperation[] operations;

    public CompositeDbOperation(final AbstractDbOperation... operations) {
        this.operations = operations;
    }

    @Override
    public void execute(final MongoDatabase connection, final Document data) {

        for (final AbstractDbOperation operation : operations) {
            operation.execute(connection, data);
        }
    }

}
