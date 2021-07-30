package com.vertx.business.services.workers;

import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class BlogVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer("BlogVerticle");

        try {
            consumer.handler(message -> {
                String operation = message.body().getString("OPERATION");
                switch (operation) {
                    case "read":
                        JsonObject query = new JsonObject();
                        query.put("_id", message.body().getValue("blogId"));
                        MongoHelper.getInstance().search("blog",query, message);
                        break;
                    case "create":
                        MongoHelper.getInstance().insert("blog", message);
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
