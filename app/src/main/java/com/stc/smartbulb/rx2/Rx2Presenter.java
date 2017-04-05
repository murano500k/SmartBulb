package com.stc.smartbulb.rx2;

import android.util.Log;

import com.stc.smartbulb.model.Device;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Single.create;

/**
 * Created by artem on 4/4/17.
 */

public class Rx2Presenter implements Rx2Contract.Presenter{
    private Rx2DeviceManager mDeviceManager;
    private Device mDevice;
    private Socket mSocket;
    private Rx2Contract.View view;
    private CompositeDisposable mDisposable;


    private static final String TAG = "Rx2Presenter";
    public Rx2Presenter(Rx2Contract.View view) {
        this.mDeviceManager = new Rx2DeviceManager();
        this.view = view;
        mDisposable = new CompositeDisposable();
        view.setPresenter(this);
    }

    @Override
    public void finish() {
        Log.i(TAG, "finish: ");
        mDisposable.dispose();
        mDevice=null;
        view.onUpdate(mDevice, "onFinish");
    }

    @Override
    public void start() {
        Log.i(TAG, "start: ");
        mDisposable.add(create((SingleOnSubscribe<Device>) e -> {
            e.setCancellable(() -> {
                mDeviceManager.cancelSearch();
            });
            mDevice=mDeviceManager.searchDevice();
            e.onSuccess(mDevice);
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Device>() {
            @Override
            public void accept(Device device) throws Exception {
                view.onUpdate(device, null);
            }
        }, throwable -> {
            throwable.printStackTrace();
                    view.onUpdate(null, throwable.getMessage());
        }));
    }

    @Override
    public void click(){
        if(mDevice==null) view.onUpdate(null,"no device");
        else sendCmd(!mDevice.isTurnedOn());
    }
    public void sendCmd(boolean cmd){
        mDisposable.add(
                Single.create((SingleOnSubscribe<Socket>) e -> { // connect
                    if(mSocket==null || mSocket.isClosed()) mSocket = mDeviceManager.connectToDevice(mDevice);
                    if(mSocket.isConnected()) e.onSuccess(mSocket);
                    else e.onError(new Throwable("Socket not connected"));
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .map(socket -> {  //send command
                    mDeviceManager.writeCmd(cmd , socket);
                    return socket;
                })
                .flatMapObservable(new Function<Socket, ObservableSource<String>>() { // readLogs
                        @Override
                        public ObservableSource<String> apply(Socket socket) throws Exception {
                            return Observable.create(e -> {
                                BufferedReader mBos= new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                                while (mSocket!=null && mSocket.isConnected()){
                                    if(mBos.ready())e.onNext(mBos.readLine());
                                }
                                e.onComplete();
                            });
                        }
                })
                .observeOn(AndroidSchedulers.mainThread())  //show result
                .subscribe(
                        s -> { //next
                            Log.d(TAG, "new line: " + s);
                            mDeviceManager.parseMsg(s, view, mDevice);
                        },
                        Throwable::printStackTrace, //error
                        () -> view.onUpdate(mDevice, null)) //complete
        );
    }
}
