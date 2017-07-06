package eu.drus.jpa.unit.mongodb.ext;

import java.util.List;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public interface Configuration {

    List<ServerAddress> getServerAddresses();

    String getDatabaseName();

    MongoClientOptions getClientOptions();

    List<MongoCredential> getCredentials();
}
