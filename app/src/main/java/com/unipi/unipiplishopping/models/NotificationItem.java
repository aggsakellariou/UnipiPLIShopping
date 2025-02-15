package com.unipi.unipiplishopping.models;

public class NotificationItem {
    private String message;
    private long timestamp;
    private String productId;

    public NotificationItem(String message, long timestamp, String productId) {
        this.message = message;
        this.timestamp = timestamp;
        this.productId = productId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}