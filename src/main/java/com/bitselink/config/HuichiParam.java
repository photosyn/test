package com.bitselink.config;

public class HuichiParam {
    public String method;
    public int syncDays;
    public String from;
    public String to;
    public long inTableInIndex;
    public long inTableOutIndex;
    public long outTableInIndex;
    public long outTableOutIndex;

    public HuichiParam() {
        method = "";
        from = "";
        to = "";
        syncDays = 7;
        inTableInIndex = -1;
        inTableOutIndex = -1;
        outTableInIndex = -1;
        outTableOutIndex = -1;
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

    public long getInTableInIndex() {
        return inTableInIndex;
    }

    public void setInTableInIndex(long inTableInIndex) {
        this.inTableInIndex = inTableInIndex;
    }

    public long getInTableOutIndex() {
        return inTableOutIndex;
    }

    public void setInTableOutIndex(long inTableOutIndex) {
        this.inTableOutIndex = inTableOutIndex;
    }

    public long getOutTableInIndex() {
        return outTableInIndex;
    }

    public void setOutTableInIndex(long outTableInIndex) {
        this.outTableInIndex = outTableInIndex;
    }

    public long getOutTableOutIndex() {
        return outTableOutIndex;
    }

    public void setOutTableOutIndex(long outTableOutIndex) {
        this.outTableOutIndex = outTableOutIndex;
    }
}
