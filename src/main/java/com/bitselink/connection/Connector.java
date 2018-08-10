package com.bitselink.connection;

import com.bitselink.LogHelper;
import com.bitselink.config.Config;
import com.bitselink.config.Site;
import com.bitselink.Client.Protocol.MsgHead;
import com.bitselink.domain.ParkingGroupData;

import java.io.InputStream;
import java.util.HashMap;
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
    private ParkingGroupData parkingGroupData;
    private MsgHead msgHead;
    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public ParkingGroupData getParkingGroupData() {
        return parkingGroupData;
    }

    public Connector() {
        isConnected = false;
        parkingGroupData = new ParkingGroupData();
        msgHead = new MsgHead();
        msgHead.setMcode("100002");
        msgHead.setVer("0001");
        msgHead.setMsgatr("20");
        msgHead.setSafeflg("11");
        msgHead.setMac("");
        parkingGroupData.getHead().add(msgHead);
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
            isConnected = connectTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isConnected;
    }

    private boolean connectTest() {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            session.selectOne("mybatis.parkingDataMapper.connectTest");
        } catch (Exception e) {
            LogHelper.warn("连接数据库失败：" + e.getMessage());
            return false;
        }finally {
            session.close();
        }
        return true;
    }

    private long checkParkingDataWithCondition(String mybatisMapper, Map condition){
        long tableIndex = -1;
        SqlSession session = sqlSessionFactory.openSession();
        try{
            parkingGroupData.getBody().addAll(session.selectList(mybatisMapper, condition));
//            parkingGroupData.getBody().addAll(session.selectList("selectTest", condition));
        } finally {
            session.close();
        }
        for (int i=0;i<parkingGroupData.getBody().size();i++){
//            parkingGroupData.getBody().get(i).setDevno(Config.rootConfig.register);
            tableIndex = Math.max(tableIndex, Long.parseLong(parkingGroupData.getBody().get(i).getRecordid()));
        }
        return tableIndex;
    }

    //检查停车场数据，如果检查结果是需要立即查询历史数据，则返回ture
    public boolean checkParkingData(){
        boolean syncHistory = false;
        long rstInTable = -1;
        long rstOutTable = -1;
        parkingGroupData.getBody().clear();
        Map condition=new HashMap();
        condition.put("devNo", Config.rootConfig.register);
        if (Config.rootConfig.syncParam.carInTableId > 0
                && Config.rootConfig.syncParam.method.equals("id")
                && !Config.syncResult.isOldTime){
            condition.put("recordId", Config.rootConfig.syncParam.carInTableId);
            LogHelper.info("抓取停车场入场数据：carIn(recordId) > " + Config.rootConfig.syncParam.carInTableId);
        }
        else {
//            condition.put("timeFrom", "2018-07-10 10:00:00");
            condition.put("timeFrom", Config.rootConfig.syncParam.from);
//            condition.put("timeTo", "2018-08-10 10:00:00");
            condition.put("timeTo", Config.rootConfig.syncParam.to);
            LogHelper.info("抓取停车场入场数据：from=" + Config.rootConfig.syncParam.from + " to=" + Config.rootConfig.syncParam.to);
        }
        rstInTable = checkParkingDataWithCondition("mybatis.parkingDataMapper.selectCarInByCondition", condition);

        condition.clear();
        condition.put("devNo", Config.rootConfig.register);
        if (Config.rootConfig.syncParam.carOutTableId > 0
                && Config.rootConfig.syncParam.method.equals("id")
                && !Config.syncResult.isOldTime){
            condition.put("recordId", Config.rootConfig.syncParam.carOutTableId);
            LogHelper.info("抓取停车场出场数据：carOut(recordId) > " + Config.rootConfig.syncParam.carOutTableId);
        }
        else {
            condition.put("timeFrom", Config.rootConfig.syncParam.from);
            condition.put("timeTo", Config.rootConfig.syncParam.to);
            LogHelper.info("抓取停车场出场数据：from=" + Config.rootConfig.syncParam.from + " to=" + Config.rootConfig.syncParam.to);
        }
        rstOutTable = checkParkingDataWithCondition("mybatis.parkingDataMapper.selectCarOutByCondition", condition);

        Config.carInTableIndex = Math.max(Config.carInTableIndex, rstInTable);
        Config.carOutTableIndex = Math.max(Config.carOutTableIndex, rstOutTable);
        //没有查到数据，更新查询条件
        if (rstInTable < 0 && rstOutTable < 0){
            //如果是同步历史数据
            if (Config.syncParamUpdate(false)) {
                syncHistory = true;
            }
        }
        return syncHistory;
    }

    public void closeDb(){

    }
}
