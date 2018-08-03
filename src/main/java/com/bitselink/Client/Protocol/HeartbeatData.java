package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class HeartbeatData {
    private List<MsgHead> head = new ArrayList<>();
    private List<HeartbeatBody> body = new ArrayList<>();

    public List<MsgHead> getHead() {
        return head;
    }

    public void setHead(List<MsgHead> head) {
        this.head = head;
    }

    public List<HeartbeatBody> getBody() {
        return body;
    }

    public void setBody(List<HeartbeatBody> body) {
        this.body = body;
    }
}
