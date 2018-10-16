package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class RetcodeData {
    private List<RespHead> head = new ArrayList<>();
    private List<RetcodeBody> body = new ArrayList<>();

    public List<RespHead> getHead() {
        return head;
    }

    public void setHead(List<RespHead> head) {
        this.head = head;
    }

    public List<RetcodeBody> getBody() {
        return body;
    }

    public void setBody(List<RetcodeBody> body) {
        this.body = body;
    }
}
