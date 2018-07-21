package com.bitselink.config;

public class SyncParam {
    public String mathod;
    public String from;
    public String to;
    public long carInTableId;
    public long carOutTableId;

    public SyncParam() {
        mathod = "";
        from = "";
        to = "";
        carInTableId = -1;
        carOutTableId = -1;
    }

    public String getMathod() {
        return mathod;
    }

    public void setMathod(String mathod) {
        this.mathod = mathod;
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
