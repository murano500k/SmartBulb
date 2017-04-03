package com.stc.smartbulb.search;

import com.stc.smartbulb.model.DeviceSearcher;

/**
 * Created by artem on 4/3/17.
 */

public class SearchPresenter implements SearchContract.Presenter {
    DeviceSearcher searcher;
    SearchPresenter() {
        searcher= new DeviceSearcher(this);
    }

    @Override
    public void searchDevice() {

    }

    @Override
    public void stopSearch() {

    }
}
