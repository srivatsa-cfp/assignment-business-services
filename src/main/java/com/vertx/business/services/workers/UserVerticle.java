package com.vertx.business.services.workers;

import com.vertx.business.services.config.ConfigObject;
import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class UserVerticle extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(UserVerticle.class);
    private final String collectionName = ConfigObject.getInstance().getConfig().getString("userCollectionName");
    private final String queryKey = "userid";

    @Override
    public void start(Promise<Void> startPromise) {
        logger.info("Starting the UserVerticle Worker Verticle");

        MessageConsumer<JsonObject> consumer = vertx.eventBus().
                consumer(Constants.USER_VERTICLE_ADDRESS.getValue());
        try {
            consumer.handler(message -> {
                String operation = message.body().getString(Constants.OPERATION.getValue());
                JsonObject query;
                switch (operation) {
                    case "register":
                        MongoHelper.getInstance().insert(collectionName, message);
                        break;
                    case "validateLogin":
                        query = new JsonObject();
                        query.put("_id", message.body().getValue(queryKey));
                        MongoHelper.getInstance().search(collectionName, query, message);
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

