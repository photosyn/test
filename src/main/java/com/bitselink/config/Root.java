package com.bitselink.config;

public class Root {
    public Site site;
    public Cloud cloud;
    public SyncParam syncParam;

    public SyncParam getSyncParam() {
        return syncParam;
    }

    public void setSyncParam(SyncParam syncParam) {
        this.syncParam = syncParam;
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
