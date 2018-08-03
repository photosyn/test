package com.bitselink.domain;

public class ParkingData {
    private String recordid;
    private String devno;
    private String inouttype;
    private String plateno;
    private String inoutdtime;

    @Override
    public String toString() {
        return "ParkingData{" +
                "recordid='" + recordid + '\'' +
                ", devno='" + devno + '\'' +
                ", inouttype='" + inouttype + '\'' +
                ", plateno='" + plateno + '\'' +
                ", inoutdtime='" + inoutdtime + '\'' +
                '}';
    }

    public String getRecordid() {
        return recordid;
    }

    public void setRecordid(String recordid) {
        this.recordid = recordid;
    }

    public String getDevno() {
        return devno;
    }

    public void setDevno(String devno) {
        this.devno = devno;
    }

    public String getInouttype() {
        return inouttype;
    }

    public void setInouttype(String inouttype) {
        this.inouttype = inouttype;
    }

    public String getPlateno() {
        return plateno;
    }

    public void setPlateno(String plateno) {
        this.plateno = plateno;
    }

    public String getInoutdtime() {
        return inoutdtime;
    }

    public void setInoutdtime(String inoutdtime) {
        this.inoutdtime = inoutdtime;
    }
}
