package com.vertx.business.services.workers;

import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class UserVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer("UserVerticle");

        try {
            consumer.handler(message -> {
                String operation = message.body().getString("OPERATION");
                JsonObject query;
                switch (operation) {
                    case "register":
                        MongoHelper.getInstance().insert("user", message);
                        break;
                    case "login":
                        query = new JsonObject();
                        query.put("_id", message.body().getValue("userid"));
                        MongoHelper.getInstance().search("user", query, message);
                        break;
                    default:
                        message.reply("Invalid Operation");
                        break;
                }

            });
        } catch (Exception ex) {

        }
    }
}

