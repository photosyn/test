package com.bitselink.connection;

import com.bitselink.config.Site;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class Connector {
    private static final String DB_ORACLE = "Oracle";
    private static final String DB_SQL_SERVER = "SQL Server";
    private static final String DB_MYSQL = "Mysql";
    private Map<String, String> mapDriverName;
    private Map<String, String> mapUrl;
    private Connection connection;

    public Connector() {
        mapDriverName = new HashMap<>();
        mapDriverName.put(DB_SQL_SERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        mapDriverName.put(DB_ORACLE, "oracle.jdbc.driver.OracleDriver");
        mapDriverName.put(DB_MYSQL, "com.mysql.cj.jdbc.Driver");
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
        System.out.println("driver:" + driverName);
        System.out.println("url" + url);
        System.out.println("user" + site.user);
        System.out.println("password" + site.password);
        try {
            Class.forName(driverName);
            connection = DriverManager.getConnection(url, site.user, site.password);
            rst = true;
        } catch (Exception db_err) {
            System.out.println(db_err.getMessage());
        }
        return rst;
    }
}
