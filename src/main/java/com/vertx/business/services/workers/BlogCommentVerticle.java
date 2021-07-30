package com.vertx.business.services.workers;

import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

public class BlogCommentVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer("BlogCommentVerticle");

        try {
            consumer.handler(message -> {
                String operation = message.body().getString("OPERATION");
                switch (operation) {
                    case "read":
                        JsonObject query = new JsonObject();
                        query.put("_id", message.body().getValue("blogId"));
                        MongoHelper.getInstance().search("commentBlog", query, message);
                        break;
                    case "create":
                        MongoHelper.getInstance().insert("commentBlog", message);
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
