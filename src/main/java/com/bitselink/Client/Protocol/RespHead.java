package com.bitselink.Client.Protocol;

public class RespHead {
    private String mcode;
    private String mid;
    private String date;
    private String time;
    private String ver;
    private String msgatr;
    private String rcode;
    private String desc;
    private String safeflg;
    private String mac;

    public String getMcode() {
        return mcode;
    }

    public void setMcode(String mcode) {
        this.mcode = mcode;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getMsgatr() {
        return msgatr;
    }

    public void setMsgatr(String msgatr) {
        this.msgatr = msgatr;
    }

    public String getRcode() {
        return rcode;
    }

    public void setRcode(String rcode) {
        this.rcode = rcode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSafeflg() {
        return safeflg;
    }

    public void setSafeflg(String safeflg) {
        this.safeflg = safeflg;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
