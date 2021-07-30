package com.vertx.business.services.helper;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoHelper {

    private static MongoHelper mongoHelper;
    private static MongoClient mongoClient;
    private MongoHelper(){

    }
    public static MongoHelper getInstance() {
        if(mongoHelper == null) {
            mongoHelper = new MongoHelper();
        }
        return mongoHelper;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void createMongoClient(Vertx vertx, JsonObject config){
        String uri = config.getString("mongo_uri") == null ? "mongodb://localhost:27017" :
                config.getString("mongo_uri");
        String db = config.getString("mongo_db") == null ? "blog": config.getString("mongo_db");

        JsonObject mongoconfig = new JsonObject()
                .put("connection_string", uri)
                .put("db_name", db)
                .put("username", "root")
                .put("password", "rootpassword")
                .put("authSource", "admin");
        mongoClient = MongoClient.createShared(vertx, mongoconfig);
    }

    public void update(String collection,JsonObject query, Message<JsonObject> message) {
        mongoClient.updateCollection(collection,query, message.body()).onComplete(r -> {
            if(r.succeeded()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("id", r.result());
                jsonObject.put("message", "success");
               message.reply(jsonObject);
            } else {
                message.fail(500, r.cause().getMessage());
            }
        });
    }

    public void insert(String collection, Message<JsonObject> message) {
        mongoClient.insert(collection, message.body()).onComplete(r -> {
            if(r.succeeded()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("id", r.result());
                jsonObject.put("message", "success");
                message.reply(jsonObject);
            } else {
                message.fail(500, r.cause().getMessage());
            }
        });
    }

    public void search(String collection, JsonObject query, Message<JsonObject> message) {
        mongoClient.find(collection, query).onComplete(r -> {
            if(r.succeeded()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.put("data", r.result());
                message.reply(jsonObject);
            } else {
                message.fail(500, r.cause().getMessage());
            }
        });
    }
}
