package com.vertx.business.services.test;

import com.vertx.business.services.config.ConfigObject;

import com.vertx.business.services.constants.Constants;
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
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class MainVerticleTest {
    Vertx vertx = Vertx.vertx();
    JsonObject configObject;

    @Test
    public void startMainVerticleServerWithSuccess() throws Throwable {
        VertxTestContext testContext = new VertxTestContext();
        vertx.createHttpServer()
                .requestHandler(req -> req.response().end())
                .listen(configObject.getInteger("port"))
                .onComplete(testContext.succeedingThenComplete());
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
        if (testContext.failed()) {
            throw testContext.causeOfFailure();
        }
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
}

