package eu.drus.jpa.unit.decorator.mongodb;

import org.bson.conversions.Bson;

import com.mongodb.client.MongoDatabase;

public class CompositeDbOperation extends AbstractDbOperation {

    private AbstractDbOperation[] operations;

    public CompositeDbOperation(final AbstractDbOperation... operations) {
        this.operations = operations;
    }

    @Override
    public void execute(final MongoDatabase connection, final Bson data) {

        for (final AbstractDbOperation operation : operations) {
            operation.execute(connection, data);
        }
    }

}
