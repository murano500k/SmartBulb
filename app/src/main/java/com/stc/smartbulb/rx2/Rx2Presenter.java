package com.stc.smartbulb.rx2;

import android.util.Log;

import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.Rx2DeviceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Single.create;

/**
 * Created by artem on 4/5/17.
 */

public class Rx2Presenter {
    private Rx2DeviceManager mDeviceManager;
    private Device mDevice;
    private Socket mSocket;

    private static final String TAG = "Rx2Presenter";
    private BufferedReader mBos;

    public Rx2Presenter() {
        this.mDeviceManager = new Rx2DeviceManager();
    }
    public Observable<Device> sendToggleCmdObservable() {
        return sendCmd(funcToggle());
    }
    public Observable<Device> getStateObservable() {
        return sendCmd(funcGetState());
    }

    public Observable<Device> sendPowerCmdObservable(boolean val) {
        return sendCmd(funcPower(val));
    }
    private Single<Device> deviceObservable(){
        if(mDevice==null) return create((SingleOnSubscribe<Device>) e -> {
            e.setCancellable(() -> mDeviceManager.cancelSearch());
            mDevice=mDeviceManager.searchDevice();
            e.onSuccess(mDevice);
        }).timeout(2, TimeUnit.SECONDS, observer -> observer.onError(new Throwable("timeout, device not found")));
        else return Single.just(mDevice);
    }
    private Single<Socket> socketObservable() {
        if (mSocket == null || mSocket.isClosed()) return deviceObservable().map(device -> {
            mDevice=device;
            mSocket = mDeviceManager.connectToDevice(mDevice);
            return mSocket;
        });
        else return Single.just(mSocket);
    }
    private Observable<Device> sendCmd(Function<Socket,Socket> cmd) {
        return socketObservable()
                .subscribeOn(Schedulers.newThread())
                .map(cmd)
                .flatMapObservable(new Function<Socket, ObservableSource<String>>() { // readLogs
                    @Override
                    public ObservableSource<String> apply(Socket socket) throws Exception {
                        return Observable.create(e -> {
                            mBos = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                            /*e.setDisposable(new Disposable() {
                                @Override
                                public void dispose() {
                                    try {
                                        mBos.close();
                                        mSocket.close();
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }

                                @Override
                                public boolean isDisposed() {
                                    return !mSocket.isClosed() ;
                                }
                            });*/
                            e.setCancellable(new Cancellable() {
                                @Override
                                public void cancel() throws Exception {
                                    mSocket.close();
                                    mBos.close();
                                }
                            });
                                while (mSocket != null && mSocket.isConnected() && !mSocket.isClosed()) {
                                    if (mBos.ready()) e.onNext(mBos.readLine());
                                }

                            e.onComplete();
                        });
                    }
                }).map(s -> mDeviceManager.parseMsg(s, mDevice));
    }


    private Function<Socket, Socket> funcGetState() {
        return socket -> {  //send command
            Log.d(TAG, "funcGetState: "+socket);
            Log.d(TAG, "funcGetState: "+mDevice);
            mDeviceManager.writeCmd(Rx2DeviceManager.CMD_GET_PROP, socket);
            return socket;
        };
    }


    Function<Socket,Socket> funcToggle(){
        Log.d(TAG, "funcToggle: ");
        return socket -> {  //send command
            mDeviceManager.writeCmd(Rx2DeviceManager.CMD_TOGGLE, socket);
            return socket;
        };
    }
    Function<Socket,Socket> funcPower(boolean val){
        Log.d(TAG, "funcPower: "+val);
        return socket -> {  //send command
            mDeviceManager.writeCmd(val, socket);
            return socket;
        };
    }
}
