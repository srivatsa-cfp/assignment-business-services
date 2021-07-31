package com.vertx.business.services.handler;

import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.JWTHelper;
import com.vertx.business.services.helper.MongoHelper;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.List;

public class CommentBlogRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(CommentBlogRequestHandler.class);

    private Vertx vertx;

    /**
     * Handles the Initializing of the router and it's associated handler.
     * <p>
     * This methods intitalizes the vertx instance for the router and creates a router object to
     * handle the Post request. Each router has the associate handler to fulfill the request and
     * failure handler to handle any failures in fulfilling the request.
     * @param Vertx instance.
     */
    public Router getRouter(Vertx vertx) throws Exception {
        this.vertx = vertx;
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/create")
                .handler(this::createCommentBlogHandler)
                .failureHandler(this::failurHandler);

        return router;
    }

    public void createCommentBlogHandler(RoutingContext context) {

        try {
            logger.info("Create CommentBlogHandler....");

            JsonObject request = context.getBodyAsJson();
            String blogId = request.getString(Constants.BLOG_ID.getValue());
            JsonObject comment = request.getJsonObject("comment");

            System.out.println("request"+ request);
            if (blogId == null) {
                JsonObject output = new JsonObject();
                output.put("message", "Bad Input");
                logger.error("Bad Input parameters....");
                context.response().setStatusCode(400).send(output.toString());
            }

            String authHeader = context.request().headers().get(HttpHeaderNames.AUTHORIZATION);
            if(JWTHelper.getInstance().validate(authHeader)) {
                String token = authHeader.split(" ")[1];
                JWTHelper.getInstance().getProvider().authenticate(
                        new JsonObject().put("token", token))
                        .onSuccess(user -> {
                            // Get the existing comments
                            request.put(Constants.OPERATION.getValue(), "read");
                            request.put("_id", blogId);

                            vertx.eventBus().request(Constants.BLOG_VERTICLE_ADDRESS.getValue(),
                                    request, result -> {
                                        JsonObject output;
                                        if (result.succeeded()) {
                                            output = (JsonObject) result.result().body();

                                            JsonObject data = output.getJsonArray("data").getJsonObject(0);

                                            JsonArray comments = data.getJsonArray("comments");
                                            comment.put("userId", user.get("sub"));
                                            comment.put("createdAt", System.currentTimeMillis());
                                            comments.add(comment);
                                            data.put("_id", blogId);
                                            data.put("comments", comments);
                                            data.put(Constants.OPERATION.getValue(), "update");

                                            vertx.eventBus().request(Constants.BLOG_VERTICLE_ADDRESS.getValue()
                                                    , data, res -> {
                                                        JsonObject resp;
                                                        if (res.succeeded()) {
                                                            resp = (JsonObject) res.result().body();
                                                            resp.getString("_id");
                                                            resp.getString("message");
                                                            context.response().setStatusCode(201).send(resp.toString());
                                                        } else {
                                                            resp = new JsonObject();
                                                            resp.put("message", "Failed to insert the comment");
                                                            resp.getString("message");
                                                            context.response().setStatusCode(500).send(resp.toString());
                                                        }
                                                    });

                                        } else {
                                            output = new JsonObject();
                                            output.put("message", "Failed to insert the document");
                                            output.getString("message");
                                            context.response().setStatusCode(500).send(output.toString());
                                        }
                                    });

                        })
                        .onFailure(err -> {
                            logger.error("Unable to handle the create comment blog handler" + err.getMessage());
                            context.response().setStatusCode(500).send(err.getMessage());
                        });
            } else {
                logger.error("Unable to handle the create comment blog handler" );
                context.response().setStatusCode(500).send("Unable to create the comment");
            }

        } catch (Exception ex) {
            logger.error("Unable to handle the create comment blog handler" + ex.getMessage());
            context.response().setStatusCode(500).send(ex.getMessage());
        }
    }

    private void failurHandler(RoutingContext context) {
        String errorCode = "400";
        String errorMessage = "API ERROR";
        context.response().setStatusCode(500).end(errorMessage);
    }

}
