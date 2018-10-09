package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class UploadConfigData {
    private List<RespHead> head = new ArrayList<>();
    private List<UploadConfigBody> body = new ArrayList<>();

    public List<RespHead> getHead() {
        return head;
    }

    public void setHead(List<RespHead> head) {
        this.head = head;
    }

    public List<UploadConfigBody> getBody() {
        return body;
    }

    public void setBody(List<UploadConfigBody> body) {
        this.body = body;
    }
}
