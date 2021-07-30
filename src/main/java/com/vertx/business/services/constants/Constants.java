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
    FAILED_DOC("Failed to insert the document"),
    BAD_INPUT("Bad Input");
    String value;
    Constants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
