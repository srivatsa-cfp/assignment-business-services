package com.vertx.business.services.workers;

import com.vertx.business.services.config.ConfigObject;
import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.HashingStrategy;

public class UserVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(UserVerticle.class);
    private final String collectionName = ConfigObject.getInstance().getConfig().getString("userCollectionName");
    private final String queryKey = "userid";
    private HashingStrategy strategy = HashingStrategy.load();

    @Override
    public void start(Promise<Void> startPromise) {

        MessageConsumer<JsonObject> consumer = vertx.eventBus().
                consumer(Constants.USER_VERTICLE_ADDRESS.getValue());
        JsonObject config = ConfigObject.getInstance().getConfig();
        try {
            consumer.handler(message -> {
                String operation = message.body().getString(Constants.OPERATION.getValue());
                JsonObject query;
                switch (operation) {
                    case "register":
                        JsonObject jsonObject = message.body();
                        String userid = jsonObject.getString("userid");
                        String password = jsonObject.getString("password");
                        jsonObject.put("createdAt", System.currentTimeMillis());
                        jsonObject.put("accountLocked", false);

                        if (userid == null || password == null) {
                            JsonObject output = new JsonObject();
                            output.put("message", "Incorrect UserId/Password");
                            logger.error("User Id or Password cannot be blank");
                            message.fail(400, "User Id or Password cannot be blank");
                        } else {
                            String hashAlgo = config.getString(Constants.HASH_ALGO.getValue());
                            String hashSalt = config.getString(Constants.HASH_SALT.getValue());
                            String hash = strategy.hash(hashAlgo, null, hashSalt, password);

                            jsonObject.put("_id", userid);
                            jsonObject.put("password", hash);
                            MongoHelper.getInstance().insert(collectionName, message);
                        }
                        break;
                    case "validateLogin":
                        JsonObject req = message.body();
                        String user = req.getString("userid");
                        String pass = req.getString("password");
                        req.put("lastLoginTime", System.currentTimeMillis());
                        req.put("loginStatus", "ACTIVE");
                        req.put("accountLocked", false);
                        req.put("_id", user);

                        if (user == null || pass == null) {
                            JsonObject output = new JsonObject();
                            output.put("message", "Incorrect UserId/Password");
                            logger.error("User Id or Password is incorrect");
                            message.fail(400,"Incorrect UserId/Password");
                        } else {
                            query = new JsonObject();
                            query.put("_id", message.body().getValue(queryKey));
                            MongoHelper.getInstance().search(collectionName, query, message);
                        }
                        break;
                    case "updateLogin":
                        logger.info("Update login");
                        query = new JsonObject();
                        query.put("_id", message.body().getValue(queryKey));
                        MongoHelper.getInstance().update(collectionName, query, message);
                        break;
                    default:
                        message.reply("Invalid Operation");
                        break;
                }
            });
        } catch (Exception ex) {
            logger.info("Starting the UserVerticle Worker Verticle"+ex.getMessage());

        }
    }
}

