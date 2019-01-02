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
import java.util.Random;

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
    public static SyncParam syncParam = new SyncParam();
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

    public static String randomMid()  {
        try {
            int len = 4;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
            String nowDate = LocalDateTime.now().format(formatter);
            StringBuffer result = new StringBuffer();
            result.append(nowDate);
            for(int i=0;i<len;i++) {
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            LogHelper.info("randomMid()异常:", e);
        }
        return null;

    }

    public static boolean read() throws JSONException{
        try {
            File file = new File(CONFIG_PATH);
            String text = FileUtils.readFileToString(file, "utf8");
            rootConfig = JSON.parseObject(text, Root.class);
            repair();
            //syncParamUpdate(true);
        } catch (IOException e) {
            LogHelper.error("配置文件缺失：" + e.getMessage() + "重新创建配置文件");
            return false;
        }
        return true;
    }

    public static boolean check(Root root) {
        if (null == root) {
            return false;
        }
        if (null == root.site || !root.site.check()) {
            return false;
        }
        if (null == root.cloud || !root.cloud.check()) {
            return false;
        }
        if (null == root.register) {
            return false;
        }
        if (null == root.databaseType) {
            return false;
        }
        if (root.databaseType.isEmpty()) {
            return false;
        }

        switch (root.databaseType) {
            case "huichi": {
                if (null == root.syncParam || !root.syncParam.check()) {
                    return false;
                }
                break;
            }
            default: {
                return false;
            }
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
        if (null == rootConfig.register) {
            rootConfig.register = new String();
        }
        if (null == rootConfig.databaseType) {
            rootConfig.databaseType = new String("huichi");
        }
        if (rootConfig.databaseType.isEmpty()) {
            rootConfig.databaseType = "huichi";
        }

        switch (rootConfig.databaseType) {
            default: {
                if (null == rootConfig.syncParam) {
                    rootConfig.syncParam = new SyncParam();
                }
                break;
            }
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

    public static boolean reset() {
        rootConfig = new Root();
        return save();
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

    private static boolean paramUpdate(boolean firstTime) {
        if (!firstTime) {
            rootConfig.syncParam.from = rootConfig.syncParam.to;
            getNextSyncTime(rootConfig.syncParam.from, 5);
            rootConfig.syncParam.to = syncResult.syncTime;

            rootConfig.syncParam.inTableInIndex = syncParam.inTableInIndex;
            rootConfig.syncParam.inTableOutIndex = syncParam.inTableOutIndex;
            rootConfig.syncParam.outTableInIndex = syncParam.outTableInIndex;
            rootConfig.syncParam.outTableOutIndex = syncParam.outTableOutIndex;
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

                rootConfig.syncParam.inTableInIndex = -1;
                rootConfig.syncParam.inTableOutIndex = -1;
                rootConfig.syncParam.outTableInIndex = -1;
                rootConfig.syncParam.outTableOutIndex = -1;
            } else {
                getNextSyncTime(rootConfig.syncParam.from, 5);
                rootConfig.syncParam.to = syncResult.syncTime;
            }
            syncParam.inTableInIndex = rootConfig.syncParam.inTableInIndex;
            syncParam.inTableOutIndex = rootConfig.syncParam.inTableOutIndex;
            syncParam.outTableInIndex = rootConfig.syncParam.outTableInIndex;
            syncParam.outTableOutIndex = rootConfig.syncParam.outTableOutIndex;
        }
        save();
        return syncResult.isOldTime;
    }

    public static boolean syncParamUpdate(boolean firstTime){
        boolean isOldTime = true;
        isOldTime = paramUpdate(firstTime);
        return isOldTime;
//        System.out.println("from:" + rootConfig.syncTime.from + " to:" + rootConfig.syncTime.to);
    }
}
