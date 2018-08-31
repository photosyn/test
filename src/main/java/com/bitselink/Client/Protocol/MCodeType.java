package com.bitselink.Client.Protocol;

public enum MCodeType {
    M_CODE_TYPE_HEART_BEAT("000001"),//心跳
    M_CODE_TYPE_REGESTER("100001"),//注册
    M_CODE_TYPE_PARK_DATA("100002"),//停车数据
    M_CODE_TYPE_INFO("100003");//异常信息

    private String msg;

    //为了更好的返回代号和说明，必须重写构造方法
    private MCodeType(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    // 根据value返回枚举类型,主要在switch中使用
    public static MCodeType getByValue(String value) {
        for (MCodeType code : values()) {
            if (code.getMsg().equals(value)) {
                return code;
            }
        }
        return null;
    }

    }
