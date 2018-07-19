package com.bitselink.connection;

import com.bitselink.config.Site;

//import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bitselink.domain.Group;
import com.bitselink.domain.GroupTest;
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
    private Connection connection;

    public Connector() {
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

    public boolean connectDb(Site site){
        boolean rst = false;
        String driverName = mapDriverName.get(site.dbType);
        String urlFormat = mapUrl.get(site.dbType);
        String url = String.format(urlFormat, site.ip, site.port, site.dbName);
        System.out.println("Connect database:");
        System.out.println("{");
        System.out.println("driver: " + driverName);
        System.out.println("url: " + url);
        System.out.println("user: " + site.user);
        System.out.println("password: " + site.password);
        System.out.println("}");
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, site.user, site.password);
            rst = true;
        } catch (Exception db_err) {
            System.out.println(db_err.getMessage());
        }
        return rst;
    }

    public boolean connectDbMybatis(Site site){
        boolean rst = false;
        try {
            //生成临时的properties文件保存mybatis连接传入的参数
            Properties properties = new Properties();
//            Properties properties = Resources.getResourceAsProperties("mybatis/sqlserver.properties");
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

//            URL database = getClass().getClassLoader().getResource("mybatis/sqlserver.properties");
//            OutputStream outputStream = new FileOutputStream(database.getFile());
//            properties.store(outputStream,"update success");

            //读取mybatis配置文件
            String resource = MYBATIS_CONFIG;
            InputStream mybatisConfig = Resources.getResourceAsStream(resource);

            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfig,"production", properties);
            SqlSession session = sqlSessionFactory.openSession();
            try{
//                GroupTest group = session.selectOne("mybatis.groupMapper.selectGroup", "0008");
                GroupTest group = session.selectOne("mybatis.groupMapper.selectGroup");
                System.out.println(group);
                rst = true;
            } finally {
                session.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rst;
    }

    public void closeDb(){
        if (null != connection)
        {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
