package com.vertx.business.services.test;

import com.vertx.business.services.config.ConfigObject;

import com.vertx.business.services.constants.Constants;
import com.vertx.business.services.helper.MongoHelper;
import com.vertx.business.services.workers.UserVerticle;
import io.vertx.core.Vertx;


import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class UserVerticleTest
{
    Vertx vertx = Vertx.vertx();
    JsonObject configObject;

    @Test
    public void failureResponseForMandatoryFields(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "register");
        String failureResponse = "User Id or Password cannot be blank";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(!result.succeeded())
                assertEquals(result.cause().getMessage(),failureResponse);
        });
        testContext.completeNow();
    }

    @Test
    public void failureResponseForMandatoryUserIdFields(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "register");
        req.put("password", "123");

        String failureResponse = "User Id or Password cannot be blank";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(!result.succeeded())
                assertEquals(result.cause().getMessage(),failureResponse);
        });
        testContext.completeNow();
    }

    @Test
    public void failureResponseForMandatoryPassword(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "register");
        req.put("userId", "123");

        String failureResponse = "User Id or Password cannot be blank";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(!result.succeeded())
                assertEquals(result.cause().getMessage(),failureResponse);
        });
        testContext.completeNow();
    }

    @Test
    public void successFullRequest(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "register");
        req.put("userid", System.currentTimeMillis());
        req.put("password", "123");
        String successFull = "success";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(result.succeeded())
                assertEquals(result.result().body(), successFull);
                testContext.completeNow();
        });
    }

    @Test
    public void duplicateRegistration(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "register");
        long userId = System.currentTimeMillis();
        req.put("userid", ""+userId);
        req.put("password", "123");
        String successFull = "success";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(result.succeeded())
                assertEquals(result.result().body(), successFull);
        });

        req.put(Constants.OPERATION.getValue(), "register");
        req.put("userid", ""+userId);
        req.put("password", "123");
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(!result.succeeded()) {
                String resp = result.cause().getMessage().substring(0,6);
                String expected = "E11000";
                assertEquals(expected, resp);
            }
            testContext.completeNow();
        });
    }

    @Test
    public void validateLoginMandatoryCheck(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "validateLogin");
        req.put("password", "123");
        String resp = "Incorrect UserId/Password";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(!result.succeeded())
                assertEquals(result.cause().getMessage(), resp);
            testContext.completeNow();
        });

    }

    @Test
    public void validateLogin(Vertx vertx, VertxTestContext testContext) {
        JsonObject req = new JsonObject();
        req.put(Constants.OPERATION.getValue(), "register");
        long userId = System.currentTimeMillis();
        req.put("userid", ""+userId);
        req.put("password", "123");
        String successFull = "success";
        vertx.eventBus().request(Constants.USER_VERTICLE_ADDRESS.getValue(), req, result -> {
            if(result.succeeded())
                assertEquals(result.result().body(), successFull);
            testContext.completeNow();
        });
    }


    @BeforeEach
    public void loadConfig() throws IOException{
        StringBuilder responseStrBuilder = new StringBuilder();
        try(InputStream is = this.getClass().getResourceAsStream("/config.json")) {
            BufferedReader bR = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = bR.readLine()) != null) {
                responseStrBuilder.append(line);
            }
            bR.close();
        }
        JsonObject r = new JsonObject(responseStrBuilder.toString());
        ConfigObject.getInstance().setConfig(r);
        configObject = ConfigObject.getInstance().getConfig();
    }

    @BeforeEach
    void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(new UserVerticle(), testContext.succeedingThenComplete());
        testContext.completeNow();
    }

    @BeforeEach
    public void loadMongoInstance() throws IOException{
        MongoHelper.getInstance().createMongoClient(vertx, configObject);
    }
}


