package com.stc.smartbulb.controller;

import android.content.Context;

/**
 * Created by artem on 3/31/17.
 */

public interface ControllerInterface {
    void searchDevice();
    void sendSwitchCommand(boolean onOff);
    void setCallback(ControllerCallback callback);
    void disconnect();
    boolean checkWifiTrigger(Context c);
}
