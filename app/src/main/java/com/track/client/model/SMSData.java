package com.track.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SMSData {
    @SerializedName("sms_id")
    @Expose
    private String sms_id;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("body")
    @Expose
    private String body;
    @SerializedName("date")
    @Expose
    private long date;
    @SerializedName("read_state")
    @Expose
    private String read_state;
    @SerializedName("delete_state")
    @Expose
    private String delete_state;
    @SerializedName("type")
    @Expose
    private String type;

    public void setSms_id(String sms_id) {
        this.sms_id = sms_id;
    }

    public String getSms_id() {
        return sms_id;
    }

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }

    public String getRead_state() {
        return read_state;
    }

    public String getDelete_state() {
        return delete_state;
    }

    public String getType() {
        return type;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setRead_state(String read_state) {
        this.read_state = read_state;
    }

    public void setDelete_state(String delete_state) {
        this.delete_state = delete_state;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
