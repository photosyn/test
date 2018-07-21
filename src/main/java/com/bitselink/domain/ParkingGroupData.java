package com.bitselink.domain;

import java.util.ArrayList;
import java.util.List;

public class ParkingGroupData {
    private int msgId;
    private String name;
    private List<ParkingData> parkingDataList = new ArrayList<>();

    @Override
    public String toString() {
        return "ParkingGroupData{" +
                "msgId=" + msgId +
                ", name='" + name + '\'' +
                ", parkingDataList=" + parkingDataList +
                '}';
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParkingData> getParkingDataList() {
        return parkingDataList;
    }

    public void setParkingDataList(List<ParkingData> parkingDataList) {
        this.parkingDataList = parkingDataList;
    }
}
