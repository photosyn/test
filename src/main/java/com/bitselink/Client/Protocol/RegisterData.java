package com.bitselink.Client.Protocol;

import com.bitselink.Client.Protocol.MsgHead;
import com.bitselink.Client.Protocol.RegisterBody;

import java.util.ArrayList;
import java.util.List;

public class RegisterData {
    private List<MsgHead> head = new ArrayList<>();
    private List<RegisterBody> body = new ArrayList<>();

    public List<MsgHead> getHead() {
        return head;
    }

    public void setHead(List<MsgHead> head) {
        this.head = head;
    }

    public List<RegisterBody> getBody() {
        return body;
    }

    public void setBody(List<RegisterBody> body) {
        this.body = body;
    }
}
