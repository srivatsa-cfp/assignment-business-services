package com.vertx.business.services.test;

import com.vertx.business.services.config.ConfigObject;
import com.vertx.business.services.helper.MongoHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.impl.EventBusImpl;
import io.vertx.core.eventbus.impl.MessageImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.print.Doc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.mock;

@ExtendWith(VertxExtension.class)
public class MongoDocumentTest {
    Vertx vertx = Vertx.vertx();
    JsonObject configObject;
    MongoHelper mongoHelper;

    @SuppressWarnings("unchecked")
    @Test
    public void insertDocument(Vertx vertx, VertxTestContext testContext) throws IOException {
        Message<JsonObject> msg = Mockito.mock(Message.class);
        JsonObject result = new JsonObject().put("blogId", "123").put("blogTitle", "test");
        Mockito.when(msg.body()).thenReturn(result);
        AsyncResult<JsonObject> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(true);
        Mockito.when(asyncResult.result()).thenReturn(result);
        mongoHelper.insert("test", msg);
        String id = msg.body().getString("_id");
        assertEquals(id.length(), 24 );
        testContext.completeNow();
    }

    @Test
    public void searchDocument(Vertx vertx, VertxTestContext testContext) throws IOException {
        Message<JsonObject> msg = Mockito.mock(Message.class);
        JsonObject result = new JsonObject().put("blogId", "123").put("blogTitle", "test");
        Mockito.when(msg.body()).thenReturn(result);
        AsyncResult<JsonObject> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(true);
        Mockito.when(asyncResult.result()).thenReturn(result);

        mongoHelper.insert("test", msg);

        JsonObject query = new JsonObject();
        query.put("blogId", "123");
        mongoHelper.search("test", query, msg);
        JsonObject jsonObject = msg.body();
        assertEquals(jsonObject.getString("_id").length(), 24 );
        testContext.completeNow();
    }

    @Test
    public void updateDocument(Vertx vertx, VertxTestContext testContext) throws IOException {
        Message<JsonObject> msg = Mockito.mock(Message.class);
        JsonObject result = new JsonObject().put("blogId", "123").put("blogTitle", "test");
        Mockito.when(msg.body()).thenReturn(result);
        AsyncResult<JsonObject> asyncResult = Mockito.mock(AsyncResult.class);
        Mockito.when(asyncResult.succeeded()).thenReturn(true);
        Mockito.when(asyncResult.result()).thenReturn(result);

        mongoHelper.insert("test", msg);

        JsonObject data = new JsonObject().put("blogId", "123").put("blogTitle", "test123");
        Mockito.when(msg.body()).thenReturn(data);

        JsonObject query = new JsonObject();
        query.put("blogId", "123");
        mongoHelper.update("test", query, msg);
        JsonObject jsonObject = msg.body();
        assertEquals(jsonObject.getString("blogTitle"), "test123" );
        testContext.completeNow();
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
    public void loadMongoInstance() throws IOException{
        MongoHelper.getInstance().createMongoClient(vertx, configObject);
        mongoHelper = MongoHelper.getInstance();
    }
}

