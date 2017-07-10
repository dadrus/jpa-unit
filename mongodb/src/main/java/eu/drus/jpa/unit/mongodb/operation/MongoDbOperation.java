package eu.drus.jpa.unit.mongodb.operation;

import org.bson.Document;

import com.mongodb.client.MongoDatabase;

public interface MongoDbOperation {
    void execute(MongoDatabase connection, Document data);
}
