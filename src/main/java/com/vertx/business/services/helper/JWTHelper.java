package com.vertx.business.services.helper;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.mongo.MongoClient;

public class JWTHelper {

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

    public void setProvider(Vertx vertx) {
        if(jwtConfig == null) {
            jwtConfig = new JWTAuthOptions()
                    .setKeyStore(new KeyStoreOptions()
                            .setType("jceks")
                            .setPath("/Users/vatsa/assignment/assignment-business-services/keystore.jceks")
                            .setPassword("secret"));
            provider = JWTAuth.create(vertx, jwtConfig);
        }
    }

    public JWTAuth getProvider() {
        return provider;
    }
}
