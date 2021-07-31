package com.vertx.business.services.test;

import com.vertx.business.services.config.ConfigObject;
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
public class ConfigTest {
    Vertx vertx = Vertx.vertx();
    JsonObject configObject;

    @BeforeEach
    public void loadConfig() throws IOException {
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

    @Test
    public void checkConfigWithJWTType(Vertx vertx, VertxTestContext testContext) throws IOException {
        assertEquals(configObject.getString("jwtType"), "jceks" );
        testContext.completeNow();
    }

    @Test
    public void checkConfigWithHashAlgo(Vertx vertx, VertxTestContext testContext) throws IOException {
        assertEquals(configObject.getString("hashAlgo"), "sha512" );
        testContext.completeNow();
    }
}
