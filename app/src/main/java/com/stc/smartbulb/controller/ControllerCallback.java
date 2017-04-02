package com.stc.smartbulb.controller;

/**
 * Created by artem on 3/29/17.
 */

public interface ControllerCallback {
    void deviceFound(Device device);
    void deviceNotFound();
    void deviceConnectedSuccessfully(Device device);
    void deviceConnectFailed(Device device);



    void setController(ControllerInterface controller);
}
