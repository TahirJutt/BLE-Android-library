package com.brainx.brainxbledemoapp;

import android.content.Context;

import com.brainx.ble_android_manager.BleManager;

import java.util.ArrayList;
import java.util.List;

public class RemoteDataManager {
    String[] buttonPressDataValue = {"X", "A", "D", "Y", "W", "U", "I"};
    String[] buttonReleaseDataValue = {"Z", "Q", "C", "T", "E", "F", "M"};

    static RemoteDataManager remoteDataManager;

    public static RemoteDataManager getInstance() {

        if (remoteDataManager == null) {
            remoteDataManager = new RemoteDataManager();
        }
        return remoteDataManager;
    }

    public void newAction(String receiveData) {
       String  receiveData1=receiveData.trim();
    }
}
