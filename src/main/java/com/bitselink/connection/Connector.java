package com.bitselink.connection;

import com.bitselink.config.Config;
import com.bitselink.config.Site;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class Connector {
    private static final String DB_ORACLE = "Oracle";
    private static final String DB_SQL_SERVER = "SQL Server";
    private static final String DB_MYSQL = "Mysql";
    //MYBATIS_CONFIG为相对Resources目录的路径
    private static final String MYBATIS_CONFIG = "mybatis/mybatis-config.xml";
    private Map<String, String> mapDriverName;
    private Map<String, String> mapUrl;
    private SqlSessionFactory sqlSessionFactory;
    private List resultList;
    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public Connector() {
        isConnected = false;
        //添加各种数据库厂商JDBC驱动名称
        mapDriverName = new HashMap<>();
        mapDriverName.put(DB_SQL_SERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        mapDriverName.put(DB_ORACLE, "oracle.jdbc.driver.OracleDriver");
        mapDriverName.put(DB_MYSQL, "com.mysql.cj.jdbc.Driver");
        //添加各种数据库厂商JDBC连接url字串
        mapUrl = new HashMap<>();
        mapUrl.put(DB_SQL_SERVER, "jdbc:sqlserver://%s:%s;DatabaseName=%s");
        mapUrl.put(DB_ORACLE, "jdbc:oracle:thin:@%s:%s:%s");
        mapUrl.put(DB_MYSQL, "jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true");
    }

    public boolean connectDbMybatis(Site site){
        //生成临时的properties文件保存mybatis连接传入的参数
        Properties properties = new Properties();
        String driverName = mapDriverName.get(site.dbType);
        properties.setProperty("driver", driverName);
        String urlFormat = mapUrl.get(site.dbType);
        String url = String.format(urlFormat, site.ip, site.port, site.dbName);
        properties.setProperty("url", url);
        properties.setProperty("username", site.user);
        properties.setProperty("password", site.password);
        System.out.println("Connect database:");
        System.out.println("{");
        System.out.println("driver: " + driverName);
        System.out.println("url: " + url);
        System.out.println("user: " + site.user);
        System.out.println("password: " + site.password);
        System.out.println("}");
        try {
            //读取mybatis配置文件
            String resource = MYBATIS_CONFIG;
            InputStream mybatisConfig = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig,"production", properties);
            isConnected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    public boolean checkParkingData(){
        boolean rst = false;
        SqlSession session = sqlSessionFactory.openSession();
        try{
            Map map=new HashMap();
            System.out.println("from:" + Config.rootConfig.syncTime.from + " to:" + Config.rootConfig.syncTime.to);
            map.put("timeFrom", Config.rootConfig.syncTime.from);
            map.put("timeTo", Config.rootConfig.syncTime.to);
//            map.put("timeFrom", "2018-07-10 11:10:00");
//            map.put("timeTo", "2018-7-10 23:59:59");
            resultList = session.selectList("mybatis.groupMapper.selectParkingData", map);
//            GroupTest group = session.selectOne("mybatis.groupMapper.selectGroup", "0008");
            if(resultList.size() > 0)
            {
                rst = true;
            }
        } finally {
            session.close();
        }
        return rst;
    }

    public String getParkingData(){
        String parkingStr = "";
        if(resultList.size() > 0)
        {
            for(int i=0;i<resultList.size();i++){
                Map map=(Map)(resultList.get(i));
                if(i != 0){
                    parkingStr += ",";
                }
                parkingStr += "{" + "\"id\":" + i + ",\"InTime\":" + map.get("InTime") + "}" + "\r\n";
            }
        }
        return parkingStr;
    }

    public void closeDb(){

    }
}
