package com.bitselink.config;

public class Root {
    public Site site;
    public Cloud cloud;
    public SyncTime syncTime;

    public SyncTime getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(SyncTime syncTime) {
        this.syncTime = syncTime;
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
