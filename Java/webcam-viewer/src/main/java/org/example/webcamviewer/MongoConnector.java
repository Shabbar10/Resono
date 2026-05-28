package org.example.webcamviewer;

import com.mongodb.client.*;
import org.bson.Document;

public class MongoConnector {
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "authDB";

    private static final MongoClient client = MongoClients.create(CONNECTION_STRING);
    public static final MongoDatabase database = client.getDatabase(DB_NAME);
    public static MongoCollection<Document> getUsersCollection() {
        return database.getCollection("users");
    }
}

