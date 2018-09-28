package com.bitselink;

import com.bitselink.Client.CloudState;

public interface ICallBack {

    void setCloudState(CloudState state, String info);
    void setParkingDataRespondReceived(boolean received);
}
