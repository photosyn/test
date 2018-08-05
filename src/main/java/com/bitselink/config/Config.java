package com.bitselink.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bitselink.LogHelper;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.WriteAbortedException;
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
    private static boolean isAppError = false;

    public static boolean isIsAppError() {
        return isAppError;
    }

    public static void setIsAppError(boolean isAppError) {
        Config.isAppError = isAppError;
    }

    public static boolean isIsWaitRegister() {
        return isWaitRegister;
    }

    public static void setIsWaitRegister(boolean isWaitRegister) {
        Config.isWaitRegister = isWaitRegister;
    }

    public static int getMsgId() {
        return msgId++;
    }

    public static boolean read() throws JSONException{
        try {
            File file = new File(CONFIG_PATH);
            String text = FileUtils.readFileToString(file, "utf8");
            rootConfig = JSON.parseObject(text, Root.class);
            syncParamUpdate(true);
        } catch (IOException e) {
            LogHelper.error("配置文件缺失：" + e.getMessage() + "重新创建配置文件");
            return false;
        }
        return true;
    }

    public static boolean repair() {
        if (null == rootConfig) {
            rootConfig = new Root();
        }
        if (null == rootConfig.site) {
            rootConfig.site = new Site();
        }
        if (null == rootConfig.cloud) {
            rootConfig.cloud = new Cloud();
        }
        if (null == rootConfig.syncParam) {
            rootConfig.syncParam = new SyncParam();
        }
        if (null == rootConfig.register) {
            rootConfig.register = new String();
        }
        return save();
    }

    public static boolean save(){
        try {
            String jsonString = JSONObject.toJSONString(rootConfig);
            File file = new File(CONFIG_PATH);
            FileUtils.writeStringToFile(file, jsonString,"utf8");
        } catch (IOException e) {
            LogHelper.error("重新创建配置文件失败：" + e.getMessage());
            return false;
        }
        return true;
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
