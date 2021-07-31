package com.vertx.business.services.helper;

import com.vertx.business.services.constants.Constants;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.mongo.MongoClient;

public class JWTHelper {

    private final Logger logger = LoggerFactory.getLogger(JWTHelper.class);

    private static JWTHelper jwtHelper;
    private JWTAuthOptions jwtConfig;
    private JWTAuth provider;

    private JWTHelper(){

    }
    public static JWTHelper getInstance() {
        if(jwtHelper == null) {
            jwtHelper = new JWTHelper();
        }
        return jwtHelper;
    }
    public void setProvider(Vertx vertx, JsonObject config) {

        logger.info("JWT TYPE" + config.getString(Constants.JWT_TYPE.getValue()));
        logger.info("JWT PATH" + config.getString(Constants.JWT_KEY_PATH.getValue()));

        if(jwtConfig == null) {
            jwtConfig = new JWTAuthOptions()
                    .setKeyStore(new KeyStoreOptions()
                            .setType(config.getString(Constants.JWT_TYPE.getValue()))
                            .setPath(config.getString(Constants.JWT_KEY_PATH.getValue()))
                            .setPassword(config.getString(Constants.JWT_SECRET.getValue())));
            provider = JWTAuth.create(vertx, jwtConfig);
        }
    }
    public JWTAuth getProvider() {
        return provider;
    }

    public String generateToken(String data) {
        return provider.generateToken(new JsonObject().put("sub", data), new JWTOptions());
    }

    public boolean validate(String authHeader) {
        if (authHeader == null || authHeader.isEmpty() ) return false;
        String[] auth = authHeader.split(" ");
        if(auth.length <=1 ) return false;
        String authType = auth[0];
        String token = auth[1];
        if (!authType.equalsIgnoreCase("Bearer")) {
           return false;
        } else if(token == null || token.length() == 0 ){
            return false;
        } else {
            return true;
        }
    }
}
