package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class VersionData {
    private List<RespHead> head = new ArrayList<>();
    private List<VersionBody> body = new ArrayList<>();

    public List<RespHead> getHead() {
        return head;
    }

    public void setHead(List<RespHead> head) {
        this.head = head;
    }

    public List<VersionBody> getBody() {
        return body;
    }

    public void setBody(List<VersionBody> body) {
        this.body = body;
    }
}
