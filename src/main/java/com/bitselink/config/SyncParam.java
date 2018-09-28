package com.bitselink.config;

public class SyncParam {
    public String method;
    public int syncDays;
    public String from;
    public String to;
    public long carInTableId;
    public long carOutTableId;

    public SyncParam() {
        method = "id";
        from = "";
        to = "";
        syncDays = 7;
        carInTableId = -1;
        carOutTableId = -1;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getSyncDays() {
        return syncDays;
    }

    public void setSyncDays(int syncDays) {
        this.syncDays = syncDays;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getCarInTableId() {
        return carInTableId;
    }

    public void setCarInTableId(long carInTableId) {
        this.carInTableId = carInTableId;
    }

    public long getCarOutTableId() {
        return carOutTableId;
    }

    public void setCarOutTableId(long carOutTableId) {
        this.carOutTableId = carOutTableId;
    }
}
