package com.stc.smartbulb.controller;

/**
 * Created by artem on 3/29/17.
 */

public interface ControllerCallback {
    void onResult(boolean result, boolean val);
}