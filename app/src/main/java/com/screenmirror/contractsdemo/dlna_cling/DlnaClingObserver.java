package com.screenmirror.contractsdemo.dlna_cling;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DlnaClingObserver implements LifecycleObserver {

    private final Context mContext;
    private final ServiceConnection mUpnpServiceConnection;
    @Nullable
    private Device selectionDevice; // Selected device

    private final DlnaClingController mController;

    /**
     * Need to call onCreate、onDestroy method
     *
     * @param context          Context object, used for service binding
     * @param registryListener Device change monitoring
     */
    public DlnaClingObserver(Context context, RegistryListener registryListener) {
        this(context, registryListener, null);
    }

    /**
     * @param context          Context object, used for service binding
     * @param registryListener Device change monitoring
     * @param owner            fragment，activity Life cycle binding, if it is empty, you need to call it yourself onCreate、onDestroy method
     */
    public DlnaClingObserver(Context context, final RegistryListener registryListener, @Nullable LifecycleOwner owner) {
        this.mContext = context;
        if (owner != null) owner.getLifecycle().addObserver(this);
        mController = new DlnaClingController();
        mUpnpServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                if (iBinder instanceof AndroidUpnpService)
                    mController.initService((AndroidUpnpService) iBinder, registryListener);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mController.setUpnpService(null);
            }
        };
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        Log.e("checkdlnaflow","create");
        mContext.getApplicationContext().bindService(new Intent(mContext, BrowserUpnpService.class), mUpnpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Service transportService = getTransportService();
        if (transportService == null) return;
        execute(new Stop(transportService) {
            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
            }
        });
        mContext.getApplicationContext().unbindService(mUpnpServiceConnection);
    }

    @Nullable
    public Device getSelectionDevice() {
        return selectionDevice;
    }

    public void setSelectionDevice(@Nullable Device device) {
        this.selectionDevice = device;
    }

    /**
     * Set source
     *
     * @param url Source address
     */
    public void autoPlay(String url, int itemType, final ExecuteCallback callback) {
        mController.autoPlay(selectionDevice, url, itemType, callback);
    }

    /**
     * Video progress adjustment
     *
     * @param seconds  Moving seconds, positive number: fast forward; negative number: fast backward
     * @param callback Screencast result callback
     */
    public void seek(int seconds, final ExecuteCallback callback) {
        Log.e("seek", "device" + selectionDevice);
        mController.seek(selectionDevice, seconds, callback);
    }

    /**
     * Volume adjustment
     *
     * @param volume   Volume adjustment number, positive/negative, minimum 0, maximum 100
     * @param callback Screencast result callback
     */
    public void setVolume(final int volume, final ExecuteCallback callback) {
        mController.setVolume(selectionDevice, volume, callback);
    }

    /**
     * Execute screencast command
     *
     * @param callback Screencast commands and callbacks
     * @see org.fourthline.cling.support.avtransport.callback
     * @see org.fourthline.cling.support.renderingcontrol.callback volume-get/set
     */
    public void execute(@NotNull ActionCallback callback) {
        mController.execute(callback);
    }

    /**
     * Get transfer service
     */
    @Nullable
    public Service getTransportService() {
        if (selectionDevice == null) return null;
        return mController.getTransportService(selectionDevice);
    }

    /**
     * Get controlled service
     */
    @Nullable
    public Service getControlService() {
        if (selectionDevice == null) return null;
        return mController.getControlService(selectionDevice);
    }

    public interface ExecuteCallback {
        void success(ActionInvocation invocation);

        void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg);
    }

    public abstract static class SimpleExecuteCallback implements ExecuteCallback {

        public abstract void callback(boolean success);

        @Override
        public void success(ActionInvocation invocation) {
            callback(true);
        }

        @Override
        public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
            callback(false);
        }
    }
}
