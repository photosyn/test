package com.bitselink.Client.Protocol;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisData {
    private List<MsgHead> head = new ArrayList<>();
    private List<DiagnosisBody> body = new ArrayList<>();

    public List<MsgHead> getHead() {
        return head;
    }

    public void setHead(List<MsgHead> head) {
        this.head = head;
    }

    public List<DiagnosisBody> getBody() {
        return body;
    }

    public void setBody(List<DiagnosisBody> body) {
        this.body = body;
    }
}
