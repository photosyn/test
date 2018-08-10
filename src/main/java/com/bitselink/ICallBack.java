package com.bitselink;

import com.bitselink.Client.CloudState;

public interface ICallBack {

    void setCloudState(CloudState state);
    void setParkingDataRespondReceived(boolean received);
}
