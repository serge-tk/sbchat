package org.example.data;

import io.vertx.core.json.Json;

public class Data {
    private String address;
    private String text;
    private byte[] picture;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return Json.encodePrettily(this);
    }
}
