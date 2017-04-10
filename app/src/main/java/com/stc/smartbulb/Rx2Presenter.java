package com.stc.smartbulb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by artem on 4/5/17.
 */

public class Rx2Presenter implements Rx2BulbContract.Presenter {
    private static final String TAG = "Rx2Presenter";

    private Rx2DeviceManager mDeviceManager;
    private Device mDevice;
    private Socket mSocket;
    private BufferedReader mBos;
    private Rx2BulbContract.View mView;
    private CompositeDisposable mDisposables;


    public Rx2Presenter(Rx2BulbContract.View view) {
        mView=view;
        view.setPresenter(this);
        this.mDeviceManager = new Rx2DeviceManager();
        mDisposables=new CompositeDisposable();
    }

    @Override
    public void sendCmd(String cmd) {
        Observable<Device> deviceObservable;
        if(mDevice!=null && cmd==Rx2DeviceManager.CMD_GET_PROP) deviceObservable=Observable.just(mDevice);
        else {

            deviceObservable = Observable.just(cmd)

                    .distinct()
                    .take(1)

                    .zipWith(socketSingle().toObservable(), (s, socket) -> {
                        if (socket == null || socket.isClosed())
                            throw new IOException("socket closed");
                        mDeviceManager.writeCmd(s, socket);
                        return socket;
                    })
                    .subscribeOn(Schedulers.io())
                    .flatMap(new Function<Socket, ObservableSource<String>>() {
                        @Override
                        public ObservableSource<String> apply(@NonNull Socket socket) throws Exception {
                            return Observable.create(e -> {
                                mBos = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                                e.setCancellable(() -> {
                                    mSocket.close();
                                    mBos.close();
                                });
                                while (mSocket != null && mSocket.isConnected() && !mSocket.isClosed()) {
                                    if (mBos.ready()) e.onNext(mBos.readLine());
                                }
                            });
                        }
                    })
                    .map(s -> {
                        if (mDevice == null) throw new NullPointerException("device null");
                        return mDeviceManager.parseMsg(s, mDevice);
                    });
        }

        deviceObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        device -> mView.newState(device, null),
                        throwable -> {
                            throwable.printStackTrace();
                            mView.newState(null, throwable.getMessage());
                        }
                );
    }


    @Override
    public void cancel() {
        if(mDisposables !=null && !mDisposables.isDisposed()) mDisposables.dispose();
        mSocket=null;
    }

    @Override
    public boolean isRunning() {
        return !mDisposables.isDisposed();
    }

    private Single<Device> deviceSingleCallable(){
        if(mDevice==null) {
            return Single.fromCallable(() -> {
                mDevice=mDeviceManager.searchDevice();
                return mDevice;
            })
                    .timeout(2, TimeUnit.SECONDS, observer -> observer.onError(new Throwable("timeout, device not found")))
                    .subscribeOn(Schedulers.io());
        }
        else return Single.just(mDevice);
    }
    private Single<Socket> socketSingle() {
        if (mSocket == null || mSocket.isClosed()) return deviceSingleCallable().map(device -> {
            mSocket = mDeviceManager.connectToDevice(device);
            return mSocket;
        });
        else return Single.just(mSocket);
    }
}
