package com.vertx.business.services.handler;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class CommentBlogRequestHandler {

    private Vertx vertx;
    public Router getRouter(Vertx vertx) {
        this.vertx = vertx;
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/create")
                .handler(this::createCommentBlogHandler)
                .failureHandler(this::failurHandler);

        router.post("/read")
                .handler(this::readCommentBlogHandler)
                .failureHandler(this::failurHandler);
        return router;
    }

    public void readCommentBlogHandler(RoutingContext context) {
        JsonObject jsonObject = context.getBodyAsJson();
        String blogId = jsonObject.getString("blogid");

        if(blogId == null) {
            JsonObject output = new JsonObject();
            output.put("message", "Bad Input");
            context.response().setStatusCode(400).send(output.toString());
        }
        jsonObject.put("OPERATION", "read");
        jsonObject.put("_id", blogId);

        vertx.eventBus().request("BlogCommentVerticle",jsonObject, result -> {
            JsonObject output;
            if(result.succeeded()) {
                output = (JsonObject) result.result().body();
                output.getString("message");
                context.response().setStatusCode(201).send(output.toString());
            } else {
                output = new JsonObject();
                output.put("message", "Failed to insert the document");
                output.getString("message");
                context.response().setStatusCode(500).send(output.toString());
            }
        });
    }


    public void createCommentBlogHandler(RoutingContext context) {

        JsonObject jsonObject = context.getBodyAsJson();
        String blogId = jsonObject.getString("blogid");
        String comment = jsonObject.getString("comment");
        jsonObject.put("createdAt", System.currentTimeMillis());

        if(context.getCookie("Authorization") == null) {
            JsonObject output = new JsonObject();
            output.put("message", "Unauthorized");
            context.response().setStatusCode(401).send(output.toString());
        }

        if(blogId == null || comment == null) {
            JsonObject output = new JsonObject();
            output.put("message", "Bad Input");
            context.response().setStatusCode(400).send(output.toString());
        }
        jsonObject.put("OPERATION", "create");
        jsonObject.put("_id", blogId);

        vertx.eventBus().request("BlogCommentVerticle",jsonObject, result -> {
            JsonObject output;
            if(result.succeeded()) {
                output = (JsonObject) result.result().body();
                output.getString("_id");
                output.getString("message");
                context.response().setStatusCode(201).send(output.toString());
            } else {
                output = new JsonObject();
                output.put("message", "Failed to insert the comment");
                output.getString("message");
                context.response().setStatusCode(500).send(output.toString());
            }
        });
    }

    private void failurHandler(RoutingContext context) {
        String errorCode = "400";
        String errorMessage = "API ERROR";
        context.response().setStatusCode(500).end(errorMessage);
    }

}
