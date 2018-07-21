package com.bitselink.domain;

public class ParkingData {
    private long id;
    private String action;
    private String plateNo;
    private String time;

    @Override
    public String toString() {
        return "ParkingData{" +
                "action='" + action + '\'' +
                ", id=" + id +
                ", plateNo='" + plateNo + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
