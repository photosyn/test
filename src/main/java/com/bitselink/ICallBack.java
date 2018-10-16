package com.bitselink;

import com.bitselink.Client.CloudState;
import com.bitselink.Client.SiteState;

public interface ICallBack {

    void setCloudState(CloudState state, String info);
    void setSiteState(SiteState state, String info);
    void setParkingDataRespondReceived(boolean received);
}
