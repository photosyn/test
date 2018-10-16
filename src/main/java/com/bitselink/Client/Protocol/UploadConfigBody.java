package com.bitselink.Client.Protocol;

public class UploadConfigBody {
    private String devno;
    private String config;

    public String getDevno() {
        return devno;
    }

    public void setDevno(String devno) {
        this.devno = devno;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
