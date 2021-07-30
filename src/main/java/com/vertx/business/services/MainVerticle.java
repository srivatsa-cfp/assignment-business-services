package com.vertx.business.services;

import com.vertx.business.services.handler.BlogRequestHandler;
import com.vertx.business.services.handler.CommentBlogRequestHandler;
import com.vertx.business.services.handler.UserRequestHandler;
import com.vertx.business.services.helper.JWTHelper;
import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        // Create the Mongo Instance
        JsonObject config = vertx.getOrCreateContext().config();
        MongoHelper.getInstance().createMongoClient(vertx, config);
        JWTHelper.getInstance().setProvider(vertx);

        // Deploy the worker verticles
        DeploymentOptions options = new DeploymentOptions().setWorker(true).setWorkerPoolSize(10);
        vertx.deployVerticle("com.vertx.business.services.workers.UserVerticle",options);
        vertx.deployVerticle("com.vertx.business.services.workers.BlogVerticle",options);
        vertx.deployVerticle("com.vertx.business.services.workers.BlogCommentVerticle",options);


        // Create a router object.
        Router router = Router.router(vertx);
        router.mountSubRouter("/v1/blog", new BlogRequestHandler().getRouter(vertx));
        router.mountSubRouter("/v1/comment", new CommentBlogRequestHandler().getRouter(vertx));
        router.mountSubRouter("/v1/user", new UserRequestHandler().getRouter(vertx));

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx.createHttpServer().requestHandler(router).listen(
                // Retrieve the port from the configuration,
                // default to 8080.
                config().getInteger("http.port", 8080),
                result -> {
                    if (result.succeeded()) {
                        startPromise.complete();
                    } else {
                        startPromise.fail(result.cause());
                    }
                });
    }

    private void test(HttpServerRequest httpServerRequest) {
        System.out.println("Came Here");
        httpServerRequest.response().send("hello ");
    }
}
