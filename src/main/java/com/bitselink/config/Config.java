package com.bitselink.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Config {
    private static final String CONFIG_PATH = "sites.conf.json";
    public static Root rootConfig;
    public static long carInTableIndex = -1;
    public static long carOutTableIndex = -1;
    private static int msgId = 0;
    private static boolean isWaitRegister = false;

    public static boolean isIsWaitRegister() {
        return isWaitRegister;
    }

    public static void setIsWaitRegister(boolean isWaitRegister) {
        Config.isWaitRegister = isWaitRegister;
    }

    public static int getMsgId() {
        return msgId++;
    }

    public static void read(){
        try {
            File file = new File(CONFIG_PATH);
            String text = FileUtils.readFileToString(file, "utf8");
//            System.out.println("Read config:" + text);
            rootConfig = JSON.parseObject(text, Root.class);
            syncParamUpdate(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save(){
        try {
            String jsonString = JSONObject.toJSONString(rootConfig);
            File file = new File(CONFIG_PATH);
            FileUtils.writeStringToFile(file, jsonString,"utf8");
//            System.out.println("Write site:" + jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void syncParamUpdate(boolean firstTime){
        if (!firstTime)
        {
            rootConfig.syncParam.from = rootConfig.syncParam.to;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            rootConfig.syncParam.to = dateFormat.format(new Date());
            rootConfig.syncParam.carInTableId = carInTableIndex;
            rootConfig.syncParam.carOutTableId = carOutTableIndex;
        } else {
            if(rootConfig.syncParam.mathod.isEmpty()){
                rootConfig.syncParam.mathod = "id";
            }
            if (rootConfig.syncParam.from.isEmpty()){
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar calendar = Calendar.getInstance();
                rootConfig.syncParam.to = dateFormat.format(calendar.getTime());
                calendar.add(Calendar.DATE, -7);
                rootConfig.syncParam.from = dateFormat.format(calendar.getTime());
                rootConfig.syncParam.carInTableId = -1;
                rootConfig.syncParam.carOutTableId = -1;
            }
            carInTableIndex = rootConfig.syncParam.carInTableId;
            carOutTableIndex = rootConfig.syncParam.carOutTableId;
        }
        save();
//        System.out.println("from:" + rootConfig.syncTime.from + " to:" + rootConfig.syncTime.to);
    }
}
