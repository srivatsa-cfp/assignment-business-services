package com.vertx.business.services.handler;

import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.JWTHelper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class BlogRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(BlogRequestHandler.class);

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
        String authHeader = context.request().headers().get(HttpHeaderNames.AUTHORIZATION);
        if(JWTHelper.getInstance().validate(authHeader)) {
            String token = authHeader.split(" ")[1];
            JWTHelper.getInstance().getProvider().authenticate(
                    new JsonObject().put("token", token))
                    .onSuccess(user -> {
                        jsonObject.put(Constants.OPERATION.getValue(), Constants.CREATE.getValue());
                        jsonObject.put("_id", blogId);
                        jsonObject.put("userId", user.get("sub"));
                        jsonObject.put("comments", new JsonArray());

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
                    })
                    .onFailure(err -> {
                        logger.error("Unable to insert the blog , "+ err.getMessage());
                        context.response().setStatusCode(500).send("Unable to insert the blog");
                    });
        } else {
            logger.error("UnAuthorized User");
            context.response().setStatusCode(401).send("UnAuthorized User");
        }
    }

    private void failurHandler(RoutingContext context) {
        int errorCode = 500;
        String errorMessage = "API ERROR";
        context.response().setStatusCode(errorCode).end(errorMessage);
    }
}
