package com.stc.smartbulb;

/**
 * Created by artem on 4/6/17.
 */

public interface Rx2BulbContract {
    interface View {
        void newState(Device device , String msg);
        void setPresenter(Presenter presenter);
    }
    interface Presenter {
        void sendCmd(String cmd, boolean isConnected);
        void cancel();
        boolean isRunning();
    }
}
