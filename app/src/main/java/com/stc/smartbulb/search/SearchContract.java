package com.stc.smartbulb.search;

import com.stc.smartbulb.model.Device;

/**
 * Created by artem on 4/3/17.
 */

public interface SearchContract {
    interface View {
        void showDevice(Device device);
        void deviceNotFound();
    }
    interface Presenter {
        void searchDevice();
        void stopSearch();
    }
}
