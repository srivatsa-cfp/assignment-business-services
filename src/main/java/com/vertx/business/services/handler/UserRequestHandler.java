package com.vertx.business.services.handler;

import com.vertx.business.services.config.ConfigObject;
import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.JWTHelper;
import io.vertx.core.Vertx;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.impl.CookieImpl;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.HashingStrategy;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class UserRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(UserRequestHandler.class);

    private Vertx vertx;
    private HashingStrategy strategy = HashingStrategy.load();
    private JsonObject config;

    public Router getRouter(Vertx vertx) {
        this.vertx = vertx;
        this.config = ConfigObject.getInstance().getConfig();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/login")
                .handler(this::loginHandler)
                .failureHandler(this::failurHandler);

        router.post("/register")
                .handler(this::registerHandler)
                .failureHandler(this::failurHandler);
        return router;
    }

    public void registerHandler(RoutingContext context) {
        logger.info("User Register Request Handler");
        try {
            JsonObject jsonObject = context.getBodyAsJson();
            jsonObject.put(Constants.OPERATION.getValue(), "register");
            logger.info("Sending event to User Verticle Address");
            vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(),
                    jsonObject, result -> {
                        if (result.succeeded()) {
                            logger.info("Successfully registered the user");
                            context.response().setStatusCode(201).send("Successfully registered the user");
                        } else {
                            logger.error("Unable to register the user");
                            context.response().setStatusCode(500).send("Failed to register the user");
                        }
                    });
        } catch (Exception ex){
            logger.info("Error in User Registration Handler"+ ex.getMessage());
            context.response().setStatusCode(500).send(ex.getMessage());
        }
    }

    public void loginHandler(RoutingContext context) {
        logger.info("User Register Request Handler");
        try {
            JsonObject jsonObject = context.getBodyAsJson();
            jsonObject.put(Constants.OPERATION.getValue(), "validateLogin");
            String userid = jsonObject.getString("userid");
            String password = jsonObject.getString("password");
            logger.info("Sending event to User Verticle Address");
            vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(),
                    jsonObject, result -> {
                JsonObject output;
                if (result.succeeded()) {
                    output = (JsonObject) result.result().body();
                    JsonObject userDocument = output.getJsonArray(Constants.DATA.getValue()).getJsonObject(0);
                    String docUserId = userDocument.getString("userid");
                    String docPassword = userDocument.getString("password");

                    if (docUserId != null && docPassword != null && strategy.verify(docPassword, password)
                            && docUserId.equalsIgnoreCase(userid)) {
                        logger.info("Successful in validating login credentials and generating the jwt token");
                        String token = null;
                        try {
                             token = JWTHelper.getInstance().generateToken(docUserId);
                        } catch (Exception ex) {
                            logger.info("failure in generating the jwt token");
                            context.response().setStatusCode(500).send("Failure in generating the token");
                        }
                        if(token != null) {

                            userDocument.put(Constants.OPERATION.getValue(), "updateLogin");
                            userDocument.put("lastLoginTime", System.currentTimeMillis());
                            userDocument.put("loginStatus", Constants.ACTIVE.getValue());
                            userDocument.put("accountLocked", false);

                            logger.info("Publishing the event to update login status");

                            vertx.eventBus().publish(Constants.USER_VERTICLE_ADDRESS.getValue(), userDocument);

                            Cookie cookie = new CookieImpl(Constants.AUTHORIZATION.getValue(), token);
                            context.addCookie(cookie);
                            context.response().setStatusCode(200).send("Logged in Successfully");
                        } else {
                            context.response().setStatusCode(500).send("Failure in generating the token");
                        }
                    } else {
                        context.response().setStatusCode(400).send("Invalid User/Id password");
                    }
                } else {
                    output = new JsonObject();
                    output.put("message", result.cause().getMessage());
                    output.getString("message");
                    context.response().setStatusCode(500).send(output.toString());
                }
            });
        } catch (Exception ex) {
            logger.error("Exception in Login Handler "+ ex.getMessage());
            context.response().setStatusCode(500).send(ex.getMessage());
        }
    }

    private void failurHandler(RoutingContext context) {
        int errorCode = 500;
        String errorMessage = "API ERROR";
        context.response().setStatusCode(errorCode).end(errorMessage);
    }
}

