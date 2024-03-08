package com.inventics.e_commerce.model;

public class cartModel {
    String pro_key;
    int qty;
    String timestamp;

    cartModel(){

    }

    public cartModel(String pro_key, int qty, String timestamp) {
        this.pro_key = pro_key;
        this.qty = qty;
        this.timestamp = timestamp;
    }

    public String getPro_key() {
        return pro_key;
    }

    public void setPro_key(String pro_key) {
        this.pro_key = pro_key;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
