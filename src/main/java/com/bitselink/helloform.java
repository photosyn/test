package com.bitselink;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONException;
import com.bitselink.Client.CloudState;
import com.bitselink.Client.EchoClient;
import com.bitselink.config.*;
import com.bitselink.connection.Connector;

public class helloform implements ICallBack {
    @Override
    public void setCloudState(CloudState state) {
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
                echoClient.sendRegisterData(echoClient.getChannel());
            }
            break;
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
            }
            else {
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
                doSiteConnect();
                //doConnectTest();
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

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                echoClient.sendParkingData(connector.checkParkingData());
            }
        };
        timer = new Timer(10000, actionListener);
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
        if (!Pattern.matches(IP_PATTERN, textFieldIp.getText())) {
            JOptionPane.showMessageDialog(null, labelIp.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

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
        if (!Pattern.matches(IP_PATTERN, cloudIP)) {
            JOptionPane.showMessageDialog(null, labelCloudIp.getText() + "格式错误", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

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
                setCloudState(CloudState.NO_REGISTERED);
                LogHelper.info("使用新4G卡号，重新走注册流程：" + cloudPhone);
            }
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
            echoClient.sendParkingData(connector.checkParkingData());
            timer.start();
        } else {
            labelConnectInfo.setText(resourceBundle.getString("msg.connectFault"));
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
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.site"), connectTab);
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
        statusTab.setLayout(new GridBagLayout());
        Font statusTabFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 28, statusTab.getFont());
        if (statusTabFont != null) statusTab.setFont(statusTabFont);
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.statusInfo"), statusTab);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        statusTab.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        statusTab.add(spacer4, gbc);
        labelSettingsResult = new JLabel();
        Font labelSettingsResultFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 28, labelSettingsResult.getFont());
        if (labelSettingsResultFont != null) labelSettingsResult.setFont(labelSettingsResultFont);
        labelSettingsResult.setText("");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        statusTab.add(labelSettingsResult, gbc);
        labelSettings = new JLabel();
        Font labelSettingsFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.BOLD, 28, labelSettings.getFont());
        if (labelSettingsFont != null) labelSettings.setFont(labelSettingsFont);
        this.$$$loadLabelText$$$(labelSettings, ResourceBundle.getBundle("myProp").getString("ui.deviceStatus"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        statusTab.add(labelSettings, gbc);
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
            super.windowClosing(e);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("helloform");
        frame.setContentPane(new helloform().topPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.addWindowListener(new CloseWindowListener());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static private final String IP_PATTERN = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
    static private final String PORT_PATTERN = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$";
    static private final String PHONE_PATTERN = "^1(3|4|5|7|8)\\d{9}$";
    static private Connector connector;
    private ResourceBundle resourceBundle;
    private Timer timer;
    private EchoClient echoClient;

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
    private JLabel labelSettings;
    private JLabel labelSettingsResult;
    private JTextField textFieldStatus;

}
