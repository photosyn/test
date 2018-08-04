package com.bitselink.config;

public class Cloud {
    public String ip;
    public String port;
    public String phone;

    public Cloud() {
        ip = "";
        port = "";
        phone = "";
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
