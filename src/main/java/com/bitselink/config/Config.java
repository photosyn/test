package com.bitselink.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bitselink.LogHelper;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Config {
    public static class SyncResult {
        public String syncTime;
        public boolean isOldTime;

        public SyncResult() {
            syncTime = "";
            isOldTime = true;
        }
    }

    public static SyncResult syncResult = new Config.SyncResult();
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

    private static void getNextSyncTime(String from, long minutes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime afterDay = LocalDateTime.parse(from, formatter).plusMinutes(minutes);
        LocalDateTime now = LocalDateTime.now();
        if (afterDay.isAfter(now)) {
            syncResult.isOldTime = false;
            syncResult.syncTime = now.format(formatter);
        } else {
            syncResult.isOldTime = true;
            syncResult.syncTime = afterDay.format(formatter);
        }
    }

    public static boolean syncParamUpdate(boolean firstTime){
        if (!firstTime)
        {
            rootConfig.syncParam.from = rootConfig.syncParam.to;
            getNextSyncTime(rootConfig.syncParam.from, 5);
            rootConfig.syncParam.to = syncResult.syncTime;

            rootConfig.syncParam.carInTableId = carInTableIndex;
            rootConfig.syncParam.carOutTableId = carOutTableIndex;
        } else {
            if(rootConfig.syncParam.method.isEmpty()){
                rootConfig.syncParam.method = "id";
            }
            if (rootConfig.syncParam.from.isEmpty()){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                if (0 >= rootConfig.syncParam.syncDays) {
                    rootConfig.syncParam.from = now.minusSeconds(10).format(formatter);
                } else {
                    rootConfig.syncParam.from = now.minusDays(rootConfig.syncParam.syncDays).format(formatter);
                }
                getNextSyncTime(rootConfig.syncParam.from, 5);
                rootConfig.syncParam.to = syncResult.syncTime;
                rootConfig.syncParam.carInTableId = -1;
                rootConfig.syncParam.carOutTableId = -1;
            } else {
                getNextSyncTime(rootConfig.syncParam.from, 5);
                rootConfig.syncParam.to = syncResult.syncTime;
            }
            carInTableIndex = rootConfig.syncParam.carInTableId;
            carOutTableIndex = rootConfig.syncParam.carOutTableId;
        }
        save();
        return syncResult.isOldTime;
//        System.out.println("from:" + rootConfig.syncTime.from + " to:" + rootConfig.syncTime.to);
    }
}
