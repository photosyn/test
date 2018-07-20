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

    public static void read(){
        try {
            File file = new File(CONFIG_PATH);
            String text = FileUtils.readFileToString(file, "utf8");
//            System.out.println("Read config:" + text);
            rootConfig = JSON.parseObject(text, Root.class);
            syncTimeUpdate(true);
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

    public static void syncTimeUpdate(boolean firstTime){
        if (rootConfig.syncTime.from.isEmpty()){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            rootConfig.syncTime.to = dateFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE, -7);
            rootConfig.syncTime.from = dateFormat.format(calendar.getTime());
            save();
        }else {
            if (!firstTime)
            {
                rootConfig.syncTime.from = rootConfig.syncTime.to;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                rootConfig.syncTime.to = dateFormat.format(new Date());
            }
        }
//        System.out.println("from:" + rootConfig.syncTime.from + " to:" + rootConfig.syncTime.to);
        save();
    }
}
