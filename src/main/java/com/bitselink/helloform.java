package com.bitselink;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Locale;
import java.util.ResourceBundle;

import com.bitselink.config.*;
import com.bitselink.connection.Connector;

public class helloform {
    public helloform() {
        //根据站点配置信息显示
        showSiteConfigData();
        resourceBundle = ResourceBundle.getBundle("myProp", new Locale("zh", "CN"));
        connector = new Connector();
        labelConnectInfo.setText(resourceBundle.getString("msg.noConnection"));

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
    }

    private void showSiteConfigData() {
        textFieldIp.setText(Root.siteConfig.ip);
        textFieldPort.setText(Root.siteConfig.port);
        textFieldUsr.setText(Root.siteConfig.user);
        textFieldPwd.setText(Root.siteConfig.password);
        textFieldDbName.setText(Root.siteConfig.dbName);
        for (int i = 0; i < comboBoxDatabase.getItemCount(); i++) {
            if (comboBoxDatabase.getItemAt(i).toString().equals(Root.siteConfig.dbType)) {
                comboBoxDatabase.setSelectedIndex(i);
            }
        }
    }

    private void saveSiteConfigData() {
        Root.siteConfig.dbType = comboBoxDatabase.getSelectedItem().toString();
        Root.siteConfig.ip = textFieldIp.getText();
        Root.siteConfig.port = textFieldPort.getText();
        Root.siteConfig.user = textFieldUsr.getText();
        Root.siteConfig.password = textFieldPwd.getText();
        Root.siteConfig.dbName = textFieldDbName.getText();
        Root.setSite();
    }

    private void doSiteConnect() {
        Site site = new Site();
        site.dbType = comboBoxDatabase.getSelectedItem().toString();
        site.ip = textFieldIp.getText();
        site.port = textFieldPort.getText();
        site.user = textFieldUsr.getText();
        site.password = textFieldPwd.getText();
        site.dbName = textFieldDbName.getText();

        if (connector.connectDb(site)) {
            labelConnectInfo.setText(resourceBundle.getString("msg.connectSuccess"));
            saveSiteConfigData();
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

    public static void main(String[] args) {
        //读取站点配置
        Root.readSite();
        JFrame frame = new JFrame("helloform");
        frame.setContentPane(new helloform().topPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private ResourceBundle resourceBundle;
    private Connector connector;

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
    private JTextField textFieldStatus;

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
        connectTab.setVisible(true);
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
        gbc.weightx = 0.3;
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
        statusTab = new JPanel();
        statusTab.setLayout(new GridBagLayout());
        Font statusTabFont = this.$$$getFont$$$("Microsoft YaHei UI", Font.PLAIN, 28, statusTab.getFont());
        if (statusTabFont != null) statusTab.setFont(statusTabFont);
        tabbedPane1.addTab(ResourceBundle.getBundle("myProp").getString("ui.statusInfo"), statusTab);
        labelIp.setLabelFor(comboBoxDatabase);
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
}
