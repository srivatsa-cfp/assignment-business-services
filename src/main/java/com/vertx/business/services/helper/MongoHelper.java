package com.vertx.business.services.helper;

import com.vertx.business.services.constants.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

public class MongoHelper {
    private final Logger logger = LoggerFactory.getLogger(MongoHelper.class);

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
        try {
            String uri = config.getString("mongoUrl") == null ? "mongodb://localhost:27017" :
                    config.getString("mongoUrl");
            logger.info("Mongo Url " + uri);
            String db = config.getString(Constants.MONGO_DATABASE_NAME.getValue()) == null ? "blog" :
                    config.getString(Constants.MONGO_DATABASE_NAME.getValue());
            logger.info("db " + db);
            String userName = config.getString(Constants.MONGO_USERNAME.getValue()) == null ? "test" :
                    config.getString(Constants.MONGO_USERNAME.getValue());

            String password = config.getString(Constants.MONGO_PASSWORD.getValue()) == null ? "test" :
                    config.getString(Constants.MONGO_PASSWORD.getValue());

            String authSource = config.getString(Constants.MONGO_AUTHSOURCE.getValue()) == null ? "test" :
                    config.getString(Constants.MONGO_AUTHSOURCE.getValue());

            JsonObject mongoconfig = new JsonObject()
                    .put(Constants.MONGO_CONNECTION_STRING.getValue(), uri)
                    .put(Constants.MONGO_DATABASE_NAME.getValue(), db)
                    .put(Constants.MONGO_USERNAME.getValue(), userName)
                    .put(Constants.MONGO_PASSWORD.getValue(), password)
                    .put(Constants.MONGO_AUTHSOURCE.getValue(), authSource);
            mongoClient = MongoClient.createShared(vertx, mongoconfig);
        } catch (Exception ex){
            logger.error("Unable to create the Mongo Instance  "+ ex.getMessage());
        }
    }

    public void update(String collection,JsonObject query, Message<JsonObject> message) {
        try {
            mongoClient.save(collection, message.body()).onComplete(r -> {
                if (r.succeeded()) {
                    logger.info("result" + r.result());
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.put("id", r.result());
                    jsonObject.put("message", "success");
                    message.reply(jsonObject);
                } else {
                    logger.error("Unable to update " + r.cause().getMessage());
                    message.fail(500, r.cause().getMessage());
                }
            });
        } catch (Exception ex){
            logger.error("Unable to update"+ ex.getMessage());

        }
    }

    public void insert(String collection, Message<JsonObject> message) {
        try {
            mongoClient.insert(collection, message.body()).onComplete(r -> {
                if (r.succeeded()) {
                    message.reply("success");
                } else {
                    logger.error("Unable to insert " + r.cause().getMessage());
                    message.fail(500, r.cause().getMessage());
                }
            });
        } catch (Exception ex){
            logger.error("Unable to insert"+ ex.getMessage());

        }
    }

    public void search(String collection, JsonObject query, Message<JsonObject> message) {
        try {
            mongoClient.find(collection, query).onComplete(r -> {
                if (r.succeeded()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.put("data", r.result());
                    message.reply(jsonObject);
                } else {
                    logger.error("Unable to search " + r.cause().getMessage());
                    message.fail(500, r.cause().getMessage());
                }
            });
        }catch (Exception ex){
            logger.error("Unable to save"+ ex.getMessage());

        }
    }
}
