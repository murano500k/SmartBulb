package com.stc.smartbulb.rx2;

import com.stc.smartbulb.model.Device;

/**
 * Created by artem on 4/4/17.
 */

public interface Rx2Contract {
    interface View{
        void setPresenter(Presenter presenter);
        void deviceReady(Device device);
        void deviceLost(String errorMsg);
        void deviceNotFound(String message);
        void onResult(boolean val);
    }
    interface Presenter{
        void start();
        void finish();
        void click();
    }
}
