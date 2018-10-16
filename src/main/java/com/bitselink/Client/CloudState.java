package com.bitselink.Client;

public enum CloudState {
    NO_CONNECT("msg.noConnection", 1), NO_REGISTERED("msg.noRegistered", 2), CONNECTED("msg.connectSuccess", 3), CONNECT_FAIL("msg.connectFault", 4), REGISTER_FAIL("msg.registerFault", 5), REGISTERED("msg.connectSuccess", 6);
    // 成员变量
    private String name;
    private int index;

    // 构造方法
    private CloudState(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
