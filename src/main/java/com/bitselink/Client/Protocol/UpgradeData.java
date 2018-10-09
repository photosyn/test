package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class UpgradeData {
    private List<RespHead> head = new ArrayList<>();
    private List<UpgradeBody> body = new ArrayList<>();

    public List<RespHead> getHead() {
        return head;
    }

    public void setHead(List<RespHead> head) {
        this.head = head;
    }

    public List<UpgradeBody> getBody() {
        return body;
    }

    public void setBody(List<UpgradeBody> body) {
        this.body = body;
    }
}
