package com.bitselink.Client;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class BitLinkEncoder {
    String encode(Object object) {
        String packStr = JSON.toJSONString(object);
        String encoded = "";
        try {
            encoded = Base64.getEncoder().encodeToString(packStr.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String sendStr = String.format("%1$08d",encoded.length()) + encoded;
        return sendStr;
    }
}
