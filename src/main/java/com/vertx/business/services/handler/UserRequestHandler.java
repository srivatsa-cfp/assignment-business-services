package com.vertx.business.services.handler;

import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.JWTHelper;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.HashingAlgorithm;
import io.vertx.ext.auth.HashingStrategy;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.impl.HashingStrategyImpl;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class UserRequestHandler {

    private Vertx vertx;
    private HashingStrategy strategy = HashingStrategy.load();

    public Router getRouter(Vertx vertx) {
        this.vertx = vertx;
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/login")
                .handler(this::loginHandler)
                .failureHandler(this::failurHandler);

        router.post("/logout")
                .handler(this::logoutHandler)
                .failureHandler(this::failurHandler);

        router.post("/register")
                .handler(this::registerHandler)
                .failureHandler(this::failurHandler);
        return router;
    }

    public void registerHandler(RoutingContext context) {
        JsonObject jsonObject = context.getBodyAsJson();
        String userid = jsonObject.getString("userid");
        String password = jsonObject.getString("password");
        jsonObject.put("createdAt", System.currentTimeMillis());
        jsonObject.put("lastLoginTime", System.currentTimeMillis());
        jsonObject.put("accountLocked", false);

        if(userid == null || password == null) {
            JsonObject output = new JsonObject();
            output.put("message", "Incorrect UserId/Password");
            context.response().setStatusCode(400).send(output.toString());
        }

        String hash = strategy.hash("sha512", null, "123", password);
        jsonObject.put("_id", userid);
        jsonObject.put("OPERATION", "register");
        jsonObject.put("password",hash);

        vertx.eventBus().request("UserVerticle",jsonObject, result -> {
            JsonObject output;
            if(result.succeeded()) {
                output = (JsonObject) result.result().body();
                output.put("message", "Registered Successfully");
                context.response().setStatusCode(201).send(output.toString());
            } else {
                output = new JsonObject();
                output.put("message", result.cause().getMessage());
                output.getString("message");
                context.response().setStatusCode(500).send(output.toString());
            }
        });
    }

    public void loginHandler(RoutingContext context) {
        JsonObject jsonObject = context.getBodyAsJson();
        String userid = jsonObject.getString("userid");
        String password = jsonObject.getString("password");
        jsonObject.put("lastLoginTime", System.currentTimeMillis());
        jsonObject.put("loginStatus", "ACTIVE");
        jsonObject.put("accountLocked", false);

        if(userid == null || password == null) {
            JsonObject output = new JsonObject();
            output.put("message", "Incorrect UserId/Password");
            context.response().setStatusCode(400).send(output.toString());
        }

        jsonObject.put("OPERATION", "login");
        jsonObject.put("_id", userid);

        vertx.eventBus().request("UserVerticle",jsonObject, result -> {
            JsonObject output;
            if(result.succeeded()) {
                output = (JsonObject) result.result().body();
                String docUserId = output.getJsonArray(Constants.DATA.getValue()).getJsonObject(0).getString("userid");
                String docPassword = output.getJsonArray(Constants.DATA.getValue()).getJsonObject(0).getString("password");
                if( docUserId != null && docPassword != null && strategy.verify(docPassword, password)
                 &&  docUserId.equalsIgnoreCase(userid)) {
                    String token = JWTHelper.getInstance().getProvider().generateToken(
                            new JsonObject().put("sub", "docUserId"), new JWTOptions());
                    Cookie cookie = new CookieImpl("Authorization", token);
                    context.addCookie(cookie);
                    context.response().setStatusCode(200).send("Logged in Successfully");
                } else {
                    context.response().setStatusCode(201).send("Invalid User/Id password");
                }
            } else {
                output = new JsonObject();
                output.put("message", result.cause().getMessage());
                output.getString("message");
                context.response().setStatusCode(500).send(output.toString());
            }
        });

    }


    public void logoutHandler(RoutingContext context) {

        JsonObject jsonObject = context.getBodyAsJson();
        String userid = jsonObject.getString("userid");
        jsonObject.put("lastLogoutTime", System.currentTimeMillis());
        jsonObject.put("loginStatus", "INACTIVE");

        if(userid == null) {
            JsonObject output = new JsonObject();
            output.put("message", "Incorrect UserId");
            context.response().setStatusCode(400).send(output.toString());
        }

        jsonObject.put("OPERATION", "logout");

        vertx.eventBus().request("UserVerticle",jsonObject, result -> {
            JsonObject output;
            if(result.succeeded()) {
                output = (JsonObject) result.result().body();
                output.put("message", "Logout Successfully");
                context.response().setStatusCode(201).send(output.toString());
            } else {
                output = new JsonObject();
                output.put("message", "Failed to Logout");
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

