package com.bitselink.Client.Protocol;

import com.bitselink.config.Config;

import java.text.DateFormat;
import java.util.Date;

public class MsgHead {
    private String mcode;
    private String mid;
    private String date;
    private String time;
    private String ver;
    private String msgatr;
    private String safeflg;
    private String mac;

    public void generateIdAndTime(){
        int msgId = Config.getMsgId();
        DateFormat d1 = DateFormat.getDateInstance();
        DateFormat d2 = DateFormat.getTimeInstance();
        Date now = new Date();
        String now_date = d1.format(now);
        String now_time = d2.format(now);
        setDate(now_date);
        setTime(now_time);
        setMid(Integer.toString(msgId));
    }

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
