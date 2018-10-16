package com.bitselink.Client;

import com.alibaba.fastjson.JSON;
import com.bitselink.LogHelper;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.binary.Base64;

public class BitLinkEncoder {
    String encode(Object object) {
        String packStr = JSON.toJSONString(object);
        LogHelper.info("客户端发送数据：" + packStr);
        String encoded = "";
        try {
            encoded = Base64.encodeBase64String(packStr.getBytes("utf-8"));
//            encoded = Base64.getEncoder().encodeToString(packStr.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            LogHelper.warn("客户端encode数据失败：" + packStr);
        }
        String sendStr = String.format("%1$08d",encoded.length()) + encoded;
        return sendStr;
    }
}
