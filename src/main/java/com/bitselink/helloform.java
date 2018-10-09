package com.bitselink;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.bitselink.Client.CloudState;
import com.bitselink.Client.EchoClient;
import com.bitselink.Client.Protocol.DiagnosisBody;
import com.bitselink.config.*;
import com.bitselink.connection.Connector;
import org.apache.commons.io.FileUtils;

public class helloform implements ICallBack {
    @Override
    public void setCloudState(CloudState state, String info) {
        labelCloudInfo.setText(resourceBundle.getString(state.getName()));
        switch (state) {
            case CONNECTED: {
                if (connector.isConnected()) {
                    timer.restart();
                }
            }
            break;
            case CONNECT_FAIL: {
                timer.stop();
            }
            break;
            case NO_REGISTERED: {
            }
            break;
            case REGISTER_FAIL: {
                labelCloudInfo.setText(resourceBundle.getString(state.getName()));
                DateFormat d1 = DateFormat.getDateTimeInstance();
                Date now = new Date();
                statusInfoList.addFirst("[" + d1.format(now) + "] " + info + "\r\n");
                if (statusInfoList.size() > 8) {
                    statusInfoList.removeLast();
                }
                String statusInfoTotal = "";
                for (String statusInfo : statusInfoList) {
                    statusInfoTotal += statusInfo;
                }
                textpaneStatusInfo.setText(statusInfoTotal);
            }
            break;
        }
    }

    @Override
    public void setParkingDataRespondReceived(boolean received) {
        if (received) {
            timer.restart();
            oneTimeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkParkingData();
                }
            }, 0);
        }
    }

    public helloform() {
        //读取配置
        try {
            if (!Config.read()) {
                if (!Config.repair()) {
                    Config.setIsAppError(true);
                    labelSettingsResult.setText("重新创建配置文件失败");
                    tabbedPane1.setSelectedIndex(2);
                } else {
                    init();
                }
            } else {
                LogHelper.info("读取配置文件成功");
                init();
            }
        } catch (JSONException e) {
            LogHelper.error("配置文件格式错误：" + e.getMessage());
            Config.setIsAppError(true);
            labelSettingsResult.setText("<html>配置文件格式错误：<br/>" + e.getMessage() + "</html>");
            tabbedPane1.setSelectedIndex(2);
        }
    }

    private void init() {
        //根据站点配置信息显示
        statusInfoList = new LinkedList<String>();
        showConfigData();
        resourceBundle = ResourceBundle.getBundle("myProp", new Locale("zh", "CN"));
        labelConnectInfo.setText(resourceBundle.getString("msg.noConnection"));
        connector = new Connector();
        LogHelper.info("创建通信客户端");
        echoClient = new EchoClient(this);
        echoClient.start();

        buttonConnect.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e action event
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connector.isConnected()) {
                    JOptionPane.showMessageDialog(null, "数据库已连接", "提示", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    doSiteConnect();
                    //doConnectTest();
                }
            }
        });
        buttonCloudSave.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCouldConfigData();
            }
        });
        buttonDefault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                showMacAddr();
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "是否恢复出场设置", "提示", JOptionPane.YES_NO_OPTION)) {
                    if (Config.reset()) {
                        showConfigData();
                        JOptionPane.showMessageDialog(null, "恢复出场设置成功", "提示", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "恢复出场设置失败，请联系客服", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkParkingData();
            }
        };
        timer = new Timer(2000, actionListener);
        oneTimeTimer = new java.util.Timer();
    }

    private void checkParkingData() {
        if (connector.checkParkingData()) {
            timer.stop();
            timer.restart();
            oneTimeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkParkingData();
                }
            }, 0);
        } else {
            echoClient.sendParkingData(connector.getParkingGroupData());
        }
    }

    private void showConfigData() {
        textFieldIp.setText(Config.rootConfig.site.ip);
        textFieldPort.setText(Config.rootConfig.site.port);
        textFieldUsr.setText(Config.rootConfig.site.user);
        textFieldPwd.setText(Config.rootConfig.site.password);
        textFieldDbName.setText(Config.rootConfig.site.dbName);
        for (int i = 0; i < comboBoxDatabase.getItemCount(); i++) {
            if (comboBoxDatabase.getItemAt(i).toString().equals(Config.rootConfig.site.dbType)) {
                comboBoxDatabase.setSelectedIndex(i);
            }
        }

        textFieldCloudIp.setText(Config.rootConfig.cloud.ip);
        textFieldCloudPort.setText(Config.rootConfig.cloud.port);
        textFieldCloudPhone.setText(Config.rootConfig.cloud.phone);
    }

    private void saveSiteConfigData() {
//        if (!Pattern.matches(IP_PATTERN, textFieldIp.getText())) {
//            JOptionPane.showMessageDialog(null, labelIp.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        if (!Pattern.matches(PORT_PATTERN, textFieldPort.getText())) {
            JOptionPane.showMessageDialog(null, labelPort.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Config.rootConfig.site.dbType = comboBoxDatabase.getSelectedItem().toString();
        Config.rootConfig.site.ip = textFieldIp.getText();
        Config.rootConfig.site.port = textFieldPort.getText();
        Config.rootConfig.site.user = textFieldUsr.getText();
        Config.rootConfig.site.password = textFieldPwd.getText();
        Config.rootConfig.site.dbName = textFieldDbName.getText();
        Config.save();
    }

    private void saveCouldConfigData() {
        String cloudIP = textFieldCloudIp.getText();
//        if (!Pattern.matches(IP_PATTERN, cloudIP)) {
//            JOptionPane.showMessageDialog(null, labelCloudIp.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        String cloudPort = textFieldCloudPort.getText();
        if (!Pattern.matches(PORT_PATTERN, textFieldCloudPort.getText())) {
            JOptionPane.showMessageDialog(null, labelCloudPort.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String cloudPhone = textFieldCloudPhone.getText();
        if (!Pattern.matches(PHONE_PATTERN, textFieldCloudPhone.getText())) {
            JOptionPane.showMessageDialog(null, labelCloudPhone.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!cloudPhone.equals(Config.rootConfig.cloud.phone)) {
            int option = JOptionPane.showConfirmDialog(null, "注意：请确认是否进行4G卡变更\r\n如选择是，将在服务器端注册为新停车场，请与服务器管理员确定是否允许注册！！\r\n如选择否，则继续使用已注册的停车场业务", "提示", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option > 0) {
                textFieldCloudPhone.setText(Config.rootConfig.cloud.phone);
            } else {
                //使用新4G卡号，需要重新走注册流程
                Config.rootConfig.register = "";
                Config.setIsWaitRegister(true);
                Config.rootConfig.cloud.ip = cloudIP;
                Config.rootConfig.cloud.port = cloudPort;
                Config.rootConfig.cloud.phone = cloudPhone;
                Config.save();
                setCloudState(CloudState.NO_REGISTERED, "");
                LogHelper.info("使用新4G卡号，重新走注册流程：" + cloudPhone);
            }
        } else {
            Config.rootConfig.cloud.ip = cloudIP;
            Config.rootConfig.cloud.port = cloudPort;
            Config.save();
        }
    }

    private void doSiteConnect() {
        Site site = new Site();
        site.dbType = comboBoxDatabase.getSelectedItem().toString();
        site.ip = textFieldIp.getText();
        site.port = textFieldPort.getText();
        site.user = textFieldUsr.getText();
        site.password = textFieldPwd.getText();
        site.dbName = textFieldDbName.getText();

        if (connector.connectDbMybatis(site)) {
            labelConnectInfo.setText(resourceBundle.getString("msg.connectSuccess"));
            saveSiteConfigData();
            connector.checkParkingData();
            echoClient.sendParkingData(connector.getParkingGroupData());
            timer.start();
        } else {
            labelConnectInfo.setText(resourceBundle.getString("msg.connectFault"));
            echoClient.addDiagnosisData("连接数据库失败", DiagnosisBody.HIGH_LEVEL);
            echoClient.sendDiagnosisData();
        }
    }

    private void doConnectTest() {
        String selectDatabase = comboBoxDatabase.getSelectedItem().toString();
        String connectInfo = "no connection!";
        System.out.println(selectDatabase);
        Connection db_conn;
        if (selectDatabase.equals("SQL Server")) {
            String driver_name = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            String url = "jdbc:sqlserver://192.168.3.4:1433;DatabaseName=ocv";
            String user_name = "sa";  //默认用户名
            String user_pwd = "123456";  //密码
            try {
                Class.forName(driver_name);
                db_conn = DriverManager.getConnection(url, user_name, user_pwd);
                labelConnectInfo.setText(resourceBundle.getString("msg.connectSuccess"));
                connectInfo = "connection successful!";
//                saveSiteConfigData();
            } catch (Exception db_err) {
                connectInfo = db_err.getMessage();
                labelConnectInfo.setText(resourceBundle.getString("msg.connectFault"));
                //db_err.printStackTrace();
            }
        } else if (selectDatabase.equals("Mysql")) {
            //MYSQL
            String driver_name = "com.mysql.cj.jdbc.Driver";
            String url = "jdbc:mysql://192.168.3.4:3306/world?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true";
            String user_name = "sa";  //默认用户名
            String user_pwd = "123456";  //密码
            try {
                Class.forName(driver_name);
                db_conn = DriverManager.getConnection(url, user_name, user_pwd);
                labelConnectInfo.setText(resourceBundle.getString("msg.connectSuccess"));
                connectInfo = "connection successful!";
//                saveSiteConfigData();
            } catch (Exception db_err) {
                connectInfo = db_err.getMessage();
                labelConnectInfo.setText(resourceBundle.getString("msg.connectFault"));
                //db_err.printStackTrace();
            }
        } else if (selectDatabase.equals("Oracle")) {
            //Oracle
            String driver_name = "oracle.jdbc.driver.OracleDriver";
            String url = "jdbc:oracle:thin:@192.168.3.4:1521:xe";
            String user_name = "sys as sysdba";  //默认用户名
            String user_pwd = "123456";  //密码
            try {
                Class.forName(driver_name);
                db_conn = DriverManager.getConnection(url, user_name, user_pwd);
                labelConnectInfo.setText(resourceBundle.getString("msg.connectSuccess"));
                connectInfo = "connection successful!";
//                saveSiteConfigData();
            } catch (Exception db_err) {
                connectInfo = db_err.getMessage();
                labelConnectInfo.setText(resourceBundle.getString("msg.connectFault"));
                //db_err.printStackTrace();
            }
        } else {
            selectDatabase = "Unknown database";
        }

        System.out.println(selectDatabase + ": " + connectInfo);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));
        topPanel.setMinimumSize(new Dimension(640, 400));
        topPanel.setPreferredSize(new Dimension(640, 400));
        tabbedPane1 = new JTabbedPane();
        Font tabbedPane1Font = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 28, tabbedPane1.getFont());
        if (tabbedPane1Font != null) tabbedPane1.setFont(tabbedPane1Font);
        topPanel.add(tabbedPane1, BorderLayout.CENTER);
        connectTab = new JPanel();
        connectTab.setLayout(new GridBagLayout());
        Font connectTabFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 28, connectTab.getFont());
        if (connectTabFont != null) connectTab.setFont(connectTabFont);
        connectTab.setVisible(false);
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.parker"), connectTab);
        comboBoxDatabase = new JComboBox();
        Font comboBoxDatabaseFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, comboBoxDatabase.getFont());
        if (comboBoxDatabaseFont != null) comboBoxDatabase.setFont(comboBoxDatabaseFont);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("SQL Server");
        defaultComboBoxModel1.addElement("Oracle");
        defaultComboBoxModel1.addElement("Mysql");
        comboBoxDatabase.setModel(defaultComboBoxModel1);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(comboBoxDatabase, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        connectTab.add(spacer1, gbc);
        buttonConnect = new JButton();
        Font buttonConnectFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, buttonConnect.getFont());
        if (buttonConnectFont != null) buttonConnect.setFont(buttonConnectFont);
        this.$$$loadButtonText$$$(buttonConnect, ResourceBundle.getBundle("myProp").getString("ui.connect"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        connectTab.add(buttonConnect, gbc);
        labelPort = new JLabel();
        Font labelPortFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelPort.getFont());
        if (labelPortFont != null) labelPort.setFont(labelPortFont);
        this.$$$loadLabelText$$$(labelPort, ResourceBundle.getBundle("myProp").getString("ui.port"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(labelPort, gbc);
        labelIp = new JLabel();
        Font labelIpFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelIp.getFont());
        if (labelIpFont != null) labelIp.setFont(labelIpFont);
        this.$$$loadLabelText$$$(labelIp, ResourceBundle.getBundle("myProp").getString("ui.ip"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(labelIp, gbc);
        textFieldIp = new JTextField();
        Font textFieldIpFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldIp.getFont());
        if (textFieldIpFont != null) textFieldIp.setFont(textFieldIpFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(textFieldIp, gbc);
        textFieldPort = new JTextField();
        Font textFieldPortFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldPort.getFont());
        if (textFieldPortFont != null) textFieldPort.setFont(textFieldPortFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(textFieldPort, gbc);
        labelUsr = new JLabel();
        Font labelUsrFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelUsr.getFont());
        if (labelUsrFont != null) labelUsr.setFont(labelUsrFont);
        this.$$$loadLabelText$$$(labelUsr, ResourceBundle.getBundle("myProp").getString("ui.user"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(labelUsr, gbc);
        textFieldUsr = new JTextField();
        Font textFieldUsrFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldUsr.getFont());
        if (textFieldUsrFont != null) textFieldUsr.setFont(textFieldUsrFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(textFieldUsr, gbc);
        labelPwd = new JLabel();
        Font labelPwdFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelPwd.getFont());
        if (labelPwdFont != null) labelPwd.setFont(labelPwdFont);
        this.$$$loadLabelText$$$(labelPwd, ResourceBundle.getBundle("myProp").getString("ui.password"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(labelPwd, gbc);
        textFieldPwd = new JTextField();
        Font textFieldPwdFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldPwd.getFont());
        if (textFieldPwdFont != null) textFieldPwd.setFont(textFieldPwdFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(textFieldPwd, gbc);
        labelStatus = new JLabel();
        Font labelStatusFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelStatus.getFont());
        if (labelStatusFont != null) labelStatus.setFont(labelStatusFont);
        this.$$$loadLabelText$$$(labelStatus, ResourceBundle.getBundle("myProp").getString("ui.siteStatus"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(labelStatus, gbc);
        labelConnectInfo = new JLabel();
        Font labelConnectInfoFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelConnectInfo.getFont());
        if (labelConnectInfoFont != null) labelConnectInfo.setFont(labelConnectInfoFont);
        labelConnectInfo.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(labelConnectInfo, gbc);
        lableDbName = new JLabel();
        Font lableDbNameFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, lableDbName.getFont());
        if (lableDbNameFont != null) lableDbName.setFont(lableDbNameFont);
        this.$$$loadLabelText$$$(lableDbName, ResourceBundle.getBundle("myProp").getString("ui.dbName"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(lableDbName, gbc);
        textFieldDbName = new JTextField();
        Font textFieldDbNameFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldDbName.getFont());
        if (textFieldDbNameFont != null) textFieldDbName.setFont(textFieldDbNameFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        connectTab.add(textFieldDbName, gbc);
        cloudTab = new JPanel();
        cloudTab.setLayout(new GridBagLayout());
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.cloud"), cloudTab);
        labelCloudIp = new JLabel();
        Font labelCloudIpFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelCloudIp.getFont());
        if (labelCloudIpFont != null) labelCloudIp.setFont(labelCloudIpFont);
        labelCloudIp.setHorizontalAlignment(10);
        labelCloudIp.setHorizontalTextPosition(11);
        this.$$$loadLabelText$$$(labelCloudIp, ResourceBundle.getBundle("myProp").getString("ui.ip"));
        labelCloudIp.setVerticalAlignment(0);
        labelCloudIp.setVerticalTextPosition(0);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(labelCloudIp, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        cloudTab.add(spacer2, gbc);
        textFieldCloudIp = new JTextField();
        Font textFieldCloudIpFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldCloudIp.getFont());
        if (textFieldCloudIpFont != null) textFieldCloudIp.setFont(textFieldCloudIpFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(textFieldCloudIp, gbc);
        labelCloudPort = new JLabel();
        Font labelCloudPortFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelCloudPort.getFont());
        if (labelCloudPortFont != null) labelCloudPort.setFont(labelCloudPortFont);
        this.$$$loadLabelText$$$(labelCloudPort, ResourceBundle.getBundle("myProp").getString("ui.port"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(labelCloudPort, gbc);
        textFieldCloudPort = new JTextField();
        Font textFieldCloudPortFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldCloudPort.getFont());
        if (textFieldCloudPortFont != null) textFieldCloudPort.setFont(textFieldCloudPortFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(textFieldCloudPort, gbc);
        labelCloudStatus = new JLabel();
        Font labelCloudStatusFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelCloudStatus.getFont());
        if (labelCloudStatusFont != null) labelCloudStatus.setFont(labelCloudStatusFont);
        this.$$$loadLabelText$$$(labelCloudStatus, ResourceBundle.getBundle("myProp").getString("ui.cloudStatus"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(labelCloudStatus, gbc);
        labelCloudInfo = new JLabel();
        Font labelCloudInfoFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelCloudInfo.getFont());
        if (labelCloudInfoFont != null) labelCloudInfo.setFont(labelCloudInfoFont);
        this.$$$loadLabelText$$$(labelCloudInfo, ResourceBundle.getBundle("myProp").getString("msg.noConnection"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(labelCloudInfo, gbc);
        buttonCloudSave = new JButton();
        Font buttonCloudSaveFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, buttonCloudSave.getFont());
        if (buttonCloudSaveFont != null) buttonCloudSave.setFont(buttonCloudSaveFont);
        this.$$$loadButtonText$$$(buttonCloudSave, ResourceBundle.getBundle("myProp").getString("ui.save"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(buttonCloudSave, gbc);
        labelCloudPhone = new JLabel();
        Font labelCloudPhoneFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelCloudPhone.getFont());
        if (labelCloudPhoneFont != null) labelCloudPhone.setFont(labelCloudPhoneFont);
        this.$$$loadLabelText$$$(labelCloudPhone, ResourceBundle.getBundle("myProp").getString("ui.phone"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(labelCloudPhone, gbc);
        textFieldCloudPhone = new JTextField();
        Font textFieldCloudPhoneFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, textFieldCloudPhone.getFont());
        if (textFieldCloudPhoneFont != null) textFieldCloudPhone.setFont(textFieldCloudPhoneFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.BOTH;
        cloudTab.add(textFieldCloudPhone, gbc);
        statusTab = new JPanel();
        statusTab.setLayout(new BorderLayout(0, 0));
        Font statusTabFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 28, statusTab.getFont());
        if (statusTabFont != null) statusTab.setFont(statusTabFont);
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.statusInfo"), statusTab);
        textpaneStatusInfo = new JTextPane();
        textpaneStatusInfo.setDropMode(DropMode.USE_SELECTION);
        textpaneStatusInfo.setEditable(false);
        Font textpaneStatusInfoFont = this.$$$getFont$$$("Microsoft YaHei UI", -1, 20, textpaneStatusInfo.getFont());
        if (textpaneStatusInfoFont != null) textpaneStatusInfo.setFont(textpaneStatusInfoFont);
        textpaneStatusInfo.setText("");
        statusTab.add(textpaneStatusInfo, BorderLayout.CENTER);
        labelStatusInfo = new JLabel();
        Font labelStatusInfoFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, labelStatusInfo.getFont());
        if (labelStatusInfoFont != null) labelStatusInfo.setFont(labelStatusInfoFont);
        this.$$$loadLabelText$$$(labelStatusInfo, ResourceBundle.getBundle("myProp").getString("ui.statusInfo"));
        statusTab.add(labelStatusInfo, BorderLayout.NORTH);
        defaultTab = new JPanel();
        defaultTab.setLayout(new GridBagLayout());
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.system"), defaultTab);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        defaultTab.add(spacer3, gbc);
        buttonDefault = new JButton();
        Font buttonDefaultFont = this.$$$getFont$$$("Microsoft JhengHei UI", Font.BOLD, 36, buttonDefault.getFont());
        if (buttonDefaultFont != null) buttonDefault.setFont(buttonDefaultFont);
        this.$$$loadButtonText$$$(buttonDefault, ResourceBundle.getBundle("myProp").getString("ui.reset"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        defaultTab.add(buttonDefault, gbc);
        lableDefault = new JLabel();
        Font lableDefaultFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 36, lableDefault.getFont());
        if (lableDefaultFont != null) lableDefault.setFont(lableDefaultFont);
        this.$$$loadLabelText$$$(lableDefault, ResourceBundle.getBundle("myProp").getString("ui.defalut"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        defaultTab.add(lableDefault, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        defaultTab.add(spacer4, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

    static class CloseWindowListener extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            if (null != connector) {
                connector.closeDb();
            }
            LogHelper.info("程序关闭\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n");
            super.windowClosing(e);
        }
    }

    private static void showMacAddr() {
        Enumeration<NetworkInterface> e = null;
        try {
            e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                String macStr = "";
                NetworkInterface ni = e.nextElement();

                System.out.println("displayname: " + ni.getDisplayName());

                System.out.println("name: " + ni.getName());

                System.out.println("MTU: " + ni.getMTU());

                System.out.println("Loopback: " + ni.isLoopback());

                System.out.println("Virtual: " + ni.isVirtual());

                System.out.println("Up: " + ni.isUp());

                System.out.println("PointToPoint: " + ni.isPointToPoint());

                byte[] mac = ni.getHardwareAddress();

                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        int temp = mac[i] & 0xff;
                        String str = Integer.toHexString(temp);
                        if (str.length() == 1) {
                            str = "0" + str;
                        }
                        macStr += str.toUpperCase();
                    }
                    System.out.println("mac is " + macStr);
                } else System.out.println("mac is null");

                System.out.println("-----");

            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }
    }

    private static String getMacAddr() {
        String macStr = "";
        try {
            byte[] macAddr = NetworkInterface.getByName("eth0").getHardwareAddress();
            if (macAddr == null) {
                macAddr = NetworkInterface.getByName("eth1").getHardwareAddress();
            }

            for (int i = 0; i < macAddr.length; i++) {
                int temp = macAddr[i] & 0xff;
                String str = Integer.toHexString(temp);
                if (str.length() == 1) {
                    str = "0" + str;
                }
                macStr += str.toUpperCase();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return macStr;
    }

    public static String getUnixMACAddress() {
        String mac = "";
        BufferedReader bufferedReader = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("ifconfig eth0");
            bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String line = null;
            int index = -1;
            while ((line = bufferedReader.readLine()) != null) {
                index = line.toLowerCase().indexOf("ether");
                if (index >= 0) {
                    mac = line.substring(index + "ether".length() + 1).trim();
                    String[] strArray = mac.split(" ");
                    mac = strArray[0].trim();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bufferedReader = null;
                process = null;
            }
        }
        return mac;
    }

    private static boolean checkActiveKey() {
        String mac = "";
        String path = ACTIVE_KEY_PATH_UNIX;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            path = ACTIVE_KEY_PATH_WIN;
            mac = getMacAddr();
        } else {
            mac = getUnixMACAddress();
        }
        try {
            File file = new File(path + CONFIG_FILE_NAME);
            if (file.exists()) {
                BigInteger sha = null;
                String text = FileUtils.readFileToString(file, "utf8");
                BitLinkInfo info = JSON.parseObject(text, BitLinkInfo.class);
                mac = mac + info.getKey();
//            System.out.println("=======加密前的数据:" + key);
                MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(mac.getBytes());
                sha = new BigInteger(messageDigest.digest());
//            System.out.println("SHA加密后:" + sha.toString(32));
                if (info.getCode().equals(sha.toString(32))) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LogHelper.error("设备激活失败");
        return false;
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                LogHelper.info("程序启动，版本：" + SOFT_VER);

//                showMacAddr();
                //检查激活码
                if (!checkActiveKey()) {
                    return;
                }

                JFrame frame = new JFrame("车量监控终端");
                ImageIcon icon = new ImageIcon(this.getClass().getResource("/image/car.png"));
                frame.setIconImage(icon.getImage());
                frame.setContentPane(new helloform().topPanel);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.pack();
                frame.addWindowListener(new CloseWindowListener());
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    static public final String SOFT_VER = "V1.1(a)";
    static private final String IP_PATTERN = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
    static private final String PORT_PATTERN = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$";
    static private final String PHONE_PATTERN = "^1(3|4|5|7|8)\\d{9}$";
    private static final String CONFIG_FILE_NAME = "bitlink.json";
    private static final String ACTIVE_KEY_PATH_WIN = "";
    private static final String ACTIVE_KEY_PATH_UNIX = "/home/pi/data/";
    static private Connector connector;
    private ResourceBundle resourceBundle;
    private Timer timer;
    private EchoClient echoClient;
    private java.util.Timer oneTimeTimer;
    private LinkedList<String> statusInfoList;

    private JPanel topPanel;
    private JTabbedPane tabbedPane1;
    private JPanel connectTab;
    private JPanel statusTab;
    private JComboBox comboBoxDatabase;
    private JButton buttonConnect;
    private JLabel labelIp;
    private JTextField textFieldIp;
    private JLabel labelPort;
    private JTextField textFieldPort;
    private JLabel labelUsr;
    private JTextField textFieldUsr;
    private JLabel labelPwd;
    private JTextField textFieldPwd;
    private JLabel labelStatus;
    private JLabel labelConnectInfo;
    private JLabel lableDbName;
    private JTextField textFieldDbName;
    private JPanel cloudTab;
    private JTextField textFieldCloudIp;
    private JTextField textFieldCloudPort;
    private JLabel labelCloudIp;
    private JLabel labelCloudPort;
    private JLabel labelCloudStatus;
    private JLabel labelCloudInfo;
    private JButton buttonCloudSave;
    private JLabel labelCloudPhone;
    private JTextField textFieldCloudPhone;
    private JLabel labelSettingsResult;
    private JPanel defaultTab;
    private JButton buttonDefault;
    private JLabel lableDefault;
    private JLabel labelStatusInfo;
    private JTextPane textpaneStatusInfo;
    private JTextField textFieldStatus;

}
