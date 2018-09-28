package com.bitselink.Client.Protocol;

import com.bitselink.config.Config;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.DateFormat;
import java.util.Date;

public class DiagnosisBody {
    public static final int LOW_LEVEL = 0;
    public static final int HIGH_LEVEL = 1;

    private int errorlevel;
    private String errormsg;
    private String devno;
    private String dttime;

    public void generateTime(){
        DateFormat d1 = DateFormat.getDateTimeInstance();
        Date now = new Date();
        setDttime(d1.format(now));
    }

    public int getErrorlevel() {
        return errorlevel;
    }

    public void setErrorlevel(int errorlevel) {
        this.errorlevel = errorlevel;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public String getDevno() {
        return devno;
    }

    public void setDevno(String devno) {
        this.devno = devno;
    }

    public String getDttime() {
        return dttime;
    }

    public void setDttime(String dttime) {
        this.dttime = dttime;
    }
}
