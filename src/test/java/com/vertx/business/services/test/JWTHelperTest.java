package com.vertx.business.services.test;
import com.vertx.business.services.config.ConfigObject;
import com.vertx.business.services.helper.JWTHelper;
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
public class JWTHelperTest {

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
    public void generateToken(Vertx vertx, VertxTestContext testContext) throws IOException {
        JWTHelper.getInstance().setProvider(vertx, configObject);
        String token = JWTHelper.getInstance().generateToken("123");
        assertEquals(token.length(), 121);
        testContext.completeNow();
    }

    @Test
    public void validateSuccess(Vertx vertx, VertxTestContext testContext) throws IOException {
        JWTHelper.getInstance().setProvider(vertx, configObject);
        String data = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMiLCJpYXQiOjE2Mjc3NzEzMjd9.5Pc2ePzMa8j1gdAmJE_uat3AWHMxOGIzGH2yOpZEOi8\n";
        boolean validate = JWTHelper.getInstance().validate(data);
        assertEquals(validate, true );
        testContext.completeNow();
    }

    @Test
    public void validateFailure(Vertx vertx, VertxTestContext testContext) throws IOException {
        JWTHelper.getInstance().setProvider(vertx, configObject);
        String data = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMiLCJpYXQiOjE2Mjc3NzEzMjd9.5Pc2ePzMa8j1gdAmJE_uat3AWHMxOGIzGH2yOpZEOi8\n";
        boolean validate = JWTHelper.getInstance().validate(data);
        assertEquals(validate, false );
        testContext.completeNow();
    }

    @Test
    public void validateFailureWithoutToken(Vertx vertx, VertxTestContext testContext) throws IOException {
        JWTHelper.getInstance().setProvider(vertx, configObject);
        String data = "Bearer";
        boolean validate = JWTHelper.getInstance().validate(data);
        assertEquals(validate, false );
        testContext.completeNow();
    }

    @Test
    public void validateFailureWithInvalidToken(Vertx vertx, VertxTestContext testContext) throws IOException {
        JWTHelper.getInstance().setProvider(vertx, configObject);
        String data = "";
        boolean validate = JWTHelper.getInstance().validate(data);
        assertEquals(validate, false );
        testContext.completeNow();
    }
}
