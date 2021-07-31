package com.vertx.business.services.constants;

public enum Constants {
    DATA("data"),
    BLOG_ID("blogId"),
    BLOG_TITLE("blogTitle"),
    MESSAGE("message"),
    READ("read"),
    CREATE("create"),
    OPERATION("operation"),
    BLOG_VERTICLE_ADDRESS("BlogVerticle"),
    USER_VERTICLE_ADDRESS("UserVerticle"),
    BLOG_COMMENT_VERTICLE_ADDRESS("BlogCommentVerticle"),
    FAILED_DOC("Failed to insert the document"),
    CONFIG_PATH("config/"),
    ENV("PROFILE_NAME"),
    JWT_TYPE("jwtType"),
    JWT_SECRET("jwtSecret"),
    JWT_KEY_PATH("jwtKeyPath"),
    WORKER_POOL_SIZE("workerPoolSize"),
    CONFIG_TYPE("file"),
    MONGO_CONNECTION_STRING("connection_string"),
    MONGO_DATABASE_NAME("db_name"),
    MONGO_USERNAME("username"),
    MONGO_PASSWORD("password"),
    MONGO_AUTHSOURCE("authSource"),
    HASH_ALGO("hashAlgo"),
    HASH_SALT("salt"),
    AUTHORIZATION("Authorization"),
    ACTIVE("Active"),
    INACTIVE("InActive"),
    BAD_INPUT("Bad Input");

    String value;
    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
