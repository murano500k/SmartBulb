package com.stc.smartbulb.rx2;

import android.util.Log;

import com.stc.smartbulb.model.Device;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Single.create;

/**
 * Created by artem on 4/4/17.
 */

public class Rx2Model implements Rx2Contract.Presenter{
    Rx2DeviceManager mDeviceManager;
    Device mDevice;
    Socket mSocket;
    Rx2Contract.View view;
    CompositeDisposable mDisposable;


    private static final String TAG = "Rx2Model";
    public Rx2Model(Rx2Contract.View view) {
        this.mDeviceManager = new Rx2DeviceManager();
        this.view = view;
        mDisposable = new CompositeDisposable();
        view.setPresenter(this);
    }



    @Override
    public void finish() {
        Log.i(TAG, "finish: ");
        mDisposable.dispose();
    }

    @Override
    public void start() {
        Log.i(TAG, "start: ");
        mDisposable.add(create(new SingleOnSubscribe<Device>() {
            @Override
            public void subscribe(SingleEmitter<Device> e) throws Exception {
                e.setCancellable(() -> {
                    mDeviceManager.cancelSearch();
                });
                mDevice=mDeviceManager.searchDevice();
                e.onSuccess(mDevice);
            }
        })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Device>() {
            @Override
            public void accept(Device device) throws Exception {
                view.deviceReady(device);
            }
        }, throwable -> {
            throwable.printStackTrace();
            view.deviceNotFound(throwable.getMessage());

        }));
    }

    private void readLogs(BufferedReader mReader) throws IOException {
        Log.d(TAG, "readLogs: "+mReader.readLine());
        mDisposable.add(Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e)throws IOException{
                e.setCancellable(mReader::close);
                    while (!e.isDisposed() && mReader.ready()) {
                        String line = mReader.readLine();
                        Log.d("LOG_READER", ""+line);
                    }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread()).subscribe());
    }

    @Override
    public void click(){
        if(mDevice==null) view.deviceLost("null");
        else {
            sendCmd(!mDevice.isTurnedOn());
        }
    }
    public void sendCmd(boolean cmd){
        Single <Socket>single=Single.create(new SingleOnSubscribe<Socket>() {
            @Override
            public void subscribe(SingleEmitter<Socket> e) throws Exception {
                if(mSocket==null || mSocket.isClosed()) mSocket = mDeviceManager.connectToDevice(mDevice);
                if(mSocket.isConnected()) e.onSuccess(mSocket);
                else e.onError(new Throwable("Socket not connected"));
            }
        }).observeOn(Schedulers.newThread()).map(new Function<Socket, Socket>() {
            @Override
            public Socket apply(Socket socket) throws Exception {
                mDeviceManager.writeCmd(cmd , socket);
                return socket;
            }
        });
        single.flatMapObservable(new Function<Socket, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Socket socket) throws Exception {
                return Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        BufferedReader mBos= new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                        while (mSocket!=null && mSocket.isConnected()){
                            if(mBos.ready())e.onNext(mBos.readLine());
                        }
                        e.onComplete();
                    }
                });
            }
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, "new line: " + s);
                    }
                }, Throwable::printStackTrace, new Action() {
                    @Override
                    public void run() throws Exception {
                        view.deviceReady(mDevice);
                    }
                });

        /*.flatMapPublisher(new Function<Socket, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Socket socket) throws Exception {
                return new Publisher<String>() {
                    @Override
                    public void subscribe(Subscriber<? super String> s) {

                        BufferedReader mBos= new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                        while (mSocket!=null && mSocket.isConnected()){
                            if(mBos.ready())s.onNext(mBos.readLine());
                        }
                        s.onComplete();
                    }
                };
            }
        });*/


    }
}
