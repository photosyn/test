package com.bitselink.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.InputStream;
import java.io.File;

public class Root {
    private static final String CONFIG_PATH = "sites.conf.json";
    public static Site siteConfig;

    public Root() {
        //System.out.println(System.getProperty("user.dir"));
    }

    public static void readSite(){
        try {
//            InputStream inputStream = new FileInputStream(CONFIG_PATH);
//            String text = IOUtils.toString(inputStream, "utf8");
            File file = new File(CONFIG_PATH);
            String text = FileUtils.readFileToString(file, "utf8");
            System.out.println("Read site:" + text);
            siteConfig = JSON.parseObject(text, Site.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSite(){
        try {
            String jsonString = JSONObject.toJSONString(siteConfig);
            File file = new File(CONFIG_PATH);
            FileUtils.writeStringToFile(file, jsonString,"utf8");
            System.out.println("Write site:" + jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
