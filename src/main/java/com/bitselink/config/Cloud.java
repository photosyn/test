package com.bitselink.config;

public class Cloud {
    public String ip;
    public String port;

    public Cloud() {
        ip = "";
        port = "";
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
