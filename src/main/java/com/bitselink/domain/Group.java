package com.bitselink.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Group {
    private String P_GroupID;
    private String P_GroupName;
    private String P_AuthID;
    private int P_Level;
    private int P_Area;
    private String P_Memo;
    private String P_DataOne;
    private String P_DataTwo;
    private int P_Del;
    private String P_User;
    private Date P_DateTime;

    public String getP_GroupID() {
        return P_GroupID;
    }

    public void setP_GroupID(String p_GroupID) {
        P_GroupID = p_GroupID;
    }

    public String getP_GroupName() {
        return P_GroupName;
    }

    public void setP_GroupName(String p_GroupName) {
        P_GroupName = p_GroupName;
    }

    public String getP_AuthID() {
        return P_AuthID;
    }

    public void setP_AuthID(String p_AuthID) {
        P_AuthID = p_AuthID;
    }

    public int getP_Level() {
        return P_Level;
    }

    public void setP_Level(int p_Level) {
        P_Level = p_Level;
    }

    public int getP_Area() {
        return P_Area;
    }

    public void setP_Area(int p_Area) {
        P_Area = p_Area;
    }

    public String getP_Memo() {
        return P_Memo;
    }

    public void setP_Memo(String p_Memo) {
        P_Memo = p_Memo;
    }

    public String getP_DataOne() {
        return P_DataOne;
    }

    public void setP_DataOne(String p_DataOne) {
        P_DataOne = p_DataOne;
    }

    public String getP_DataTwo() {
        return P_DataTwo;
    }

    public void setP_DataTwo(String p_DataTwo) {
        P_DataTwo = p_DataTwo;
    }

    public int getP_Del() {
        return P_Del;
    }

    public void setP_Del(int p_Del) {
        P_Del = p_Del;
    }

    public String getP_User() {
        return P_User;
    }

    public void setP_User(String p_User) {
        P_User = p_User;
    }

    public Date getP_DateTime() {
        return P_DateTime;
    }

    public void setP_DateTime(Date p_DateTime) {
        P_DateTime = p_DateTime;
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Group [P_GroupID=" + P_GroupID + ", P_GroupName=" + P_GroupName + ", P_AuthID=" + P_AuthID
                + ", P_Level=" + P_Level + ", P_Area=" + P_Area + ", P_Memo=" + P_Memo + ", P_DataOne=" + P_DataOne
                + ", P_DataTwo=" + P_DataTwo + ", P_Del=" + P_Del + ", P_User=" + P_User + ", P_DateTime=" + sdf.format(P_DateTime)
                + "]";
    }
}
