package com.vertx.business.services.config;


import io.vertx.core.json.JsonObject;

public class ConfigObject {
    private static ConfigObject configObject;
    private static JsonObject jsonObject;

    private ConfigObject() {
    }

    public static ConfigObject getInstance() {
        if(configObject == null) {
            configObject = new ConfigObject();
        }
        return configObject;
    }

    public void setConfig(JsonObject config) {
        jsonObject = config;
    }
    public JsonObject getConfig() {
        return jsonObject;
    }
}
