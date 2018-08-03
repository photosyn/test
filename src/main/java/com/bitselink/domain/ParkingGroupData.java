package com.bitselink.domain;

import com.bitselink.Client.Protocol.MsgHead;

import java.util.ArrayList;
import java.util.List;

public class ParkingGroupData {
    private List<MsgHead> head = new ArrayList<>();
    private List<ParkingData> body = new ArrayList<>();

    @Override
    public String toString() {
        return "ParkingGroupData{" +
                "head=" + head +
                ", body=" + body +
                '}';
    }

    public List<MsgHead> getHead() {
        return head;
    }

    public void setHead(List<MsgHead> head) {
        this.head = head;
    }

    public List<ParkingData> getBody() {
        return body;
    }

    public void setBody(List<ParkingData> body) {
        this.body = body;
    }
}
