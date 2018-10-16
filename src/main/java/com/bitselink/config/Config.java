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
    public static HuichiParam huichiParam = new HuichiParam();
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
            e.printStackTrace();
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
                if (null == root.huichiParam || !root.huichiParam.check()) {
                    return false;
                }
                break;
            }
            default: {
                if (null == root.syncParam && !root.syncParam.check()) {
                    return false;
                }
                break;
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
            case "huichi": {
                if (null == rootConfig.huichiParam) {
                    rootConfig.huichiParam = new HuichiParam();
                }
                break;
            }
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

    private static boolean huichiParamUpdate(boolean firstTime) {
        if (!firstTime) {
            rootConfig.huichiParam.from = rootConfig.huichiParam.to;
            getNextSyncTime(rootConfig.huichiParam.from, 5);
            rootConfig.huichiParam.to = syncResult.syncTime;

            rootConfig.huichiParam.inTableInIndex = huichiParam.inTableInIndex;
            rootConfig.huichiParam.inTableOutIndex = huichiParam.inTableOutIndex;
            rootConfig.huichiParam.outTableInIndex = huichiParam.outTableInIndex;
            rootConfig.huichiParam.outTableOutIndex = huichiParam.outTableOutIndex;
        } else {
            if(rootConfig.huichiParam.method.isEmpty()){
                rootConfig.huichiParam.method = "id";
            }
            if (rootConfig.huichiParam.from.isEmpty()){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                if (0 >= rootConfig.huichiParam.syncDays) {
                    rootConfig.huichiParam.from = now.minusSeconds(10).format(formatter);
                } else {
                    rootConfig.huichiParam.from = now.minusDays(rootConfig.huichiParam.syncDays).format(formatter);
                }
                getNextSyncTime(rootConfig.huichiParam.from, 5);
                rootConfig.huichiParam.to = syncResult.syncTime;

                rootConfig.huichiParam.inTableInIndex = -1;
                rootConfig.huichiParam.inTableOutIndex = -1;
                rootConfig.huichiParam.outTableInIndex = -1;
                rootConfig.huichiParam.outTableOutIndex = -1;
            } else {
                getNextSyncTime(rootConfig.huichiParam.from, 5);
                rootConfig.huichiParam.to = syncResult.syncTime;
            }
            huichiParam.inTableInIndex = rootConfig.huichiParam.inTableInIndex;
            huichiParam.inTableOutIndex = rootConfig.huichiParam.inTableOutIndex;
            huichiParam.outTableInIndex = rootConfig.huichiParam.outTableInIndex;
            huichiParam.outTableOutIndex = rootConfig.huichiParam.outTableOutIndex;
        }
        save();
        return syncResult.isOldTime;
    }

    public static boolean syncParamUpdate(boolean firstTime){
        boolean isOldTime = true;
        if (rootConfig.databaseType.equals("huichi")) {
            isOldTime = huichiParamUpdate(firstTime);
        }
        return isOldTime;
//        System.out.println("from:" + rootConfig.syncTime.from + " to:" + rootConfig.syncTime.to);
    }
}
