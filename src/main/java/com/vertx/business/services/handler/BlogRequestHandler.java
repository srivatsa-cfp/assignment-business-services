package com.vertx.business.services.handler;

import com.vertx.business.services.constants.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class BlogRequestHandler {

    private Vertx vertx;
    public Router getRouter(Vertx vertx) {
        this.vertx = vertx;
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/"+Constants.CREATE.getValue())
                .handler(this::createBlogHandler)
                .failureHandler(this::failurHandler);

        router.post("/"+Constants.READ.getValue())
                .handler(this::readBlogHandler)
                .failureHandler(this::failurHandler);
        return router;
    }

    public void readBlogHandler(RoutingContext context) {
        JsonObject jsonObject = context.getBodyAsJson();
        String blogId = jsonObject.getString(Constants.BLOG_ID.getValue());

        if(blogId == null) {
            JsonObject output = new JsonObject();
            output.put(Constants.MESSAGE.getValue(), Constants.BAD_INPUT.getValue());
            context.response().setStatusCode(400).send(output.toString());
        }
        jsonObject.put("_id", blogId);
        jsonObject.put(Constants.OPERATION.getValue(), Constants.READ.getValue());

        vertx.eventBus().request(Constants.BLOG_VERTICLE_ADDRESS.getValue(),jsonObject, result -> {
            JsonObject output;
            if(result.succeeded()) {
                output = (JsonObject) result.result().body();
                output.getString("_id");
                output.getString(Constants.MESSAGE.getValue());
                context.response().setStatusCode(201).send(output.toString());
            } else {
                output = new JsonObject();
                output.put(Constants.MESSAGE.getValue(), Constants.FAILED_DOC.getValue());
                output.getString(Constants.MESSAGE.getValue());
                context.response().setStatusCode(500).send(output.toString());
            }
        });
    }


    public void createBlogHandler(RoutingContext context) {

       JsonObject jsonObject = context.getBodyAsJson();
       String blogId = jsonObject.getString(Constants.BLOG_ID.getValue());
       String blogTitle = jsonObject.getString(Constants.BLOG_TITLE.getValue());

       if(blogId == null || blogTitle == null) {
           JsonObject output = new JsonObject();
           output.put(Constants.MESSAGE.getValue(), Constants.BAD_INPUT.getValue());
           context.response().setStatusCode(400).send(output.toString());
       }
       jsonObject.put(Constants.OPERATION.getValue(), Constants.CREATE.getValue());
       jsonObject.put("_id", blogId);
       vertx.eventBus().request(Constants.BLOG_VERTICLE_ADDRESS.getValue(),jsonObject, result -> {
           JsonObject output;
           if(result.succeeded()) {
               output = (JsonObject) result.result().body();
               output.getString(Constants.MESSAGE.getValue());
               context.response().setStatusCode(201).send(output.toString());
           } else {
               output = new JsonObject();
               output.put(Constants.MESSAGE.getValue(), Constants.FAILED_DOC.getValue());
               output.getString(Constants.MESSAGE.getValue());
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
