package com.bitselink.config;

public class Root {
    public Site site;
    public Cloud cloud;
    public HuichiParam huichiParam;
    public SyncParam syncParam;
    public String register;
    public String databaseType;

    public Root() {
        site = new Site();
        cloud = new Cloud();
        register = "";
        databaseType = "";
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getRegister() {
        return register;
    }

    public void setRegister(String register) {
        this.register = register;
    }

    public HuichiParam getHuichiParam() {
        return huichiParam;
    }

    public void setHuichiParam(HuichiParam huichiParam) {
        this.huichiParam = huichiParam;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public Cloud getCloud() {
        return cloud;
    }

    public void setCloud(Cloud cloud) {
        this.cloud = cloud;
    }
}
