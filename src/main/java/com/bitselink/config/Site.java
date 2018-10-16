package com.bitselink.config;

public class Site {
    public String dbType;
    public String ip;
    public String port;
    public String user;
    public String password;
    public String dbName;

    public Site() {
        dbType = "";
        ip = "";
        port = "";
        user = "";
        password = "";
        dbName = "";
    }

    public boolean check() {
        if(dbType.isEmpty()) {
            return false;
        }
        if(ip.isEmpty()) {
            return false;
        }
        if(port.isEmpty()) {
            return false;
        }
        if(user.isEmpty()) {
            return false;
        }
        if(password.isEmpty()) {
            return false;
        }
        if(dbName.isEmpty()) {
            return false;
        }
        return true;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
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

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
}
