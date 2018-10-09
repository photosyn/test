package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class EmptyData {
    private List<RespHead> head = new ArrayList<>();
    private List<EmptyBody> body = new ArrayList<>();

    public List<RespHead> getHead() {
        return head;
    }

    public void setHead(List<RespHead> head) {
        this.head = head;
    }

    public List<EmptyBody> getBody() {
        return body;
    }

    public void setBody(List<EmptyBody> body) {
        this.body = body;
    }
}
