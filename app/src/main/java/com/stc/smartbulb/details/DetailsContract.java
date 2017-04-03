package com.stc.smartbulb.details;

import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.Trigger;

/**
 * Created by artem on 4/3/17.
 */

public interface DetailsContract {
    interface View {
        void showDevice(Device device);
        void deviceNotFound();
        void setPresenter(Presenter presenter);
        void deviceStateUpdated(boolean isEnabled);
    }
    interface Presenter {
        void connect(Device device);
        void newCmd(boolean enable);
        void disconnect();
        void isConnected();
        void addTrigger(Trigger trigger);


    }
}
