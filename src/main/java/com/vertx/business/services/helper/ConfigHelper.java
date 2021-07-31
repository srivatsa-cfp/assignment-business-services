package com.vertx.business.services.helper;

import com.vertx.business.services.constants.Constants;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.util.Map;

public class ConfigHelper {

    private static ConfigRetriever configRetriever;
    private final static Logger logger = LoggerFactory.getLogger( ConfigHelper.class );

    public static ConfigRetriever getConfigRetriever(Vertx vertx) {
        logger.info("Starting theConfigRetriever");
        try{
            ConfigStoreOptions fileStore = new ConfigStoreOptions()
                    .setType(Constants.CONFIG_TYPE.getValue())
                    .setConfig(new JsonObject().put("path",
                            Constants.CONFIG_PATH.getValue() +
                            getServerEnvironmentName() + ".json"));
            logger.info(fileStore);
            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
            configRetriever = ConfigRetriever.create(vertx, options);
            logger.info(configRetriever);
            return configRetriever;
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error in ConfigRetriever {} ", ex.getCause());
        }
        return configRetriever;
    }

    public static String getServerEnvironmentName() {
        Map<String, String> env = System.getenv();
        String envName = env.get(Constants.ENV.getValue());
        return envName == null ? "dev":  envName;
    }
}
