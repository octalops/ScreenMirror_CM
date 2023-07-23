package com.screenmirror.contractsdemo.dlna_cling;

import android.text.TextUtils;
import android.util.Log;

import com.screenmirror.contractsdemo.ui.filelist.adapter.MediaAdapter;

import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.Icon;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDAServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo;
import org.fourthline.cling.support.avtransport.callback.Play;
import org.fourthline.cling.support.avtransport.callback.Seek;
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI;
import org.fourthline.cling.support.avtransport.callback.Stop;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume;
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DlnaClingController {

    @Nullable
    private AndroidUpnpService mUpnpService;

    private static final ServiceType AV_TRANSPORT_SERVICE = new UDAServiceType("AVTransport");
    private static final ServiceType RENDERING_CONTROL_SERVICE = new UDAServiceType("RenderingControl");

    private static final String SERVER_TYPE = "MediaServer";
    private static final int SERVER_VERSION = 1;

    /**
     * 初始化投屏服务
     *
     * @param upnpService      投屏服务
     * @param registryListener 设备变化监听
     */
    public void initService(AndroidUpnpService upnpService, RegistryListener registryListener) {
        mUpnpService = upnpService;
        if (mUpnpService == null) return;
        final UDN udn = UpnpUtil.uniqueSystemIdentifier("GNaP-MediaServer");
        if (mUpnpService.getRegistry().getLocalDevice(udn, true) != null) {
//                    LocalService service = localDevice1.findService(new UDAServiceType(SERVER_TYPE, SERVER_VERSION));
            return;
        }
        try {
            DeviceType type = new UDADeviceType(SERVER_TYPE, SERVER_VERSION);
            DeviceDetails details = new DeviceDetails("DMS  (" + android.os.Build.MODEL + ")", new ManufacturerDetails(
                    android.os.Build.MANUFACTURER), new ModelDetails(android.os.Build.MODEL, "MSI MediaServer", "v1"));
            LocalService service = new AnnotationLocalServiceBinder().read(SwitchPower.class);
            service.setManager(new DefaultServiceManager(service, SwitchPower.class));
            LocalDevice localDevice = new LocalDevice(new DeviceIdentity(udn), type, details, (Icon) null, service);
            mUpnpService.getRegistry().addDevice(localDevice);
        } catch (ValidationException e) {
            e.printStackTrace();
        }
        mUpnpService.getRegistry().addListener(registryListener);
        mUpnpService.getControlPoint().search();
    }

    /**
     * 设置片源
     *
     * @param device   投屏设备
     * @param url      片源地址
     * @param itemType 投屏类型
     * @param callback 投屏结果回调
     * @see UpnpUtil itemType
     */
    public void setAVTransportURI(@Nullable Device device, @Nullable String url, int itemType,
                                  @Nullable final DlnaClingObserver.ExecuteCallback callback) {
        if (TextUtils.isEmpty(url)) {
            executeError("Projection address is abnormal", callback);
            return;
        }
        final Service avtService = getTransportService(device, callback);
        if (avtService == null) return;

        String metadata = UpnpUtil.pushMediaToRender(url, "", "", "", itemType);
        execute(new SetAVTransportURI(avtService, url, metadata) {
            @Override
            public void success(ActionInvocation invocation) {
                execute(new Play(avtService) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        super.success(invocation);
                        if (callback != null) callback.success(invocation);
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                        Log.d("error", s);
                        if (callback != null)
                            callback.failure(actionInvocation, upnpResponse, s);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                if (callback != null) callback.failure(invocation, operation, defaultMsg);
            }
        });
    }

    /**
     * Play video
     *
     * @param device   projection device
     * @param url      source address
     * @param itemType Projection type
     * @param callback Projection result callback
     * @see UpnpUtil itemType
     */
    public void autoPlay(@Nullable Device device, @Nullable final String url, final int itemType,
                         @Nullable final DlnaClingObserver.ExecuteCallback callback) {
        if (TextUtils.isEmpty(url)) {
            executeError("Projection address is abnormal", callback);
            return;
        }
        if (mUpnpService == null || mUpnpService.getControlPoint() == null) {
            executeError("Mirroring service is abnormal", callback);
            return;
        }
        final Service avtService = getTransportService(device, callback);
        if (avtService == null) return;
        // Stop first, then set the source, and finally play


        execute(new Stop(avtService) {
            @Override
            public void success(ActionInvocation invocation) {

                String metadata = UpnpUtil.pushMediaToRender(url, "12", MediaAdapter.mediaName, "", itemType);
                Log.e("metaData", "" + metadata);


                Log.d("data", metadata);
                execute(new SetAVTransportURI(avtService, url, metadata) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        execute(new Play(avtService) {
                            @Override
                            public void success(ActionInvocation invocation) {
                                super.success(invocation);
                                if (callback != null) callback.success(invocation);
                            }

                            @Override
                            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                                Log.d("error", s);
                                if (callback != null)
                                    callback.failure(actionInvocation, upnpResponse, s);
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
                        Log.d("error", defaultMsg);
                        if (callback != null) callback.failure(invocation, operation, defaultMsg);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                Log.d("error", s);
                if (callback != null) callback.failure(actionInvocation, upnpResponse, s);
            }
        });

    }


    /**
     * Video progress adjustment
     *
     * @param device   Screen projection equipment
     * @param seconds  Moving seconds, positive number: fast forward; negative number: fast backward
     * @param callback Projection result callback
     */
    public void seek(Device device, final int seconds, final DlnaClingObserver.ExecuteCallback callback) {
        final Service transportService = getTransportService(device, callback);
        if (transportService == null) return;
        execute(new GetPositionInfo(transportService) {
            @Override
            public void received(ActionInvocation actionInvocation, PositionInfo positionInfo) {
                Log.d("Data", positionInfo.toString());
                if (positionInfo.getTrackDuration().equals("00:00:00")) {
                    executeError("The current video does not support progress adjustment", callback);
                    Log.d("error", "trackissue");
                    return;
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date date = null;
                try {
                    date = dateFormat.parse(positionInfo.getRelTime());
                } catch (ParseException ignored) {
                }
                if (date == null) {
                    Log.d("error", "empty data");
                    executeError("The current video does not support progress adjustment", callback);
                    return;
                }
                String toTime = dateFormat.format(date);
                if (seconds < 0) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(date.getTime());
                    Calendar toCalendar = Calendar.getInstance();
                    toCalendar.setTimeInMillis(date.getTime() + (seconds * 1000));
                    if (toCalendar.get(Calendar.YEAR) > calendar.get(Calendar.YEAR) ||
                            toCalendar.get(Calendar.DAY_OF_YEAR) > calendar.get(Calendar.DAY_OF_YEAR)) {
                        toTime = "00:00:00";
                    }
                }
                final String time = toTime;
                execute(new Seek(transportService, time) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        if (callback != null) callback.success(invocation);
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                        if (callback != null) callback.failure(actionInvocation, upnpResponse, s);
                        Log.d("error", actionInvocation.toString());
                        Log.d("error", upnpResponse.getResponseDetails());
                        Log.d("error", s);

                    }
                });
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                if (callback != null) callback.failure(actionInvocation, upnpResponse, s);
            }
        });
    }

    /**
     * 音量调节
     *
     * @param device   Screen projection equipment
     * @param volume   Volume adjustment number, positive/negative, minimum 0, maximum 100
     * @param callback Projection result callback
     */
    public void setVolume(Device device, final int volume, final DlnaClingObserver.ExecuteCallback callback) {
        final Service service = getControlService(device, callback);
        if (service == null) return;
        execute(new GetVolume(service) {
            @Override
            public void received(ActionInvocation actionInvocation, final int i) {
                int toVolume = Math.max(0, Math.min(100, i + volume));
                execute(new SetVolume(service, toVolume) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        if (callback != null) callback.success(actionInvocation);
                    }

                    @Override
                    public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                        if (callback != null) callback.failure(actionInvocation, upnpResponse, s);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation actionInvocation, UpnpResponse upnpResponse, String s) {
                if (callback != null) callback.failure(actionInvocation, upnpResponse, s);
            }
        });
    }

    /**
     * Execute screencast command
     *
     * @param callback Screencast commands and callbacks
     * @see org.fourthline.cling.support.avtransport.callback
     * @see org.fourthline.cling.support.renderingcontrol.callback volume-get/set
     */
    public void execute(final ActionCallback callback) {
        if (mUpnpService == null || mUpnpService.getControlPoint() == null) {
            callback.failure(null, new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED), "Mirroring service is abnormal\n");
            return;
        }
        mUpnpService.getControlPoint().execute(callback);
    }

    /**
     * Get the equipment service of the projection screen
     */
    @Nullable
    private Service getTransportService(@Nullable Device device, @Nullable DlnaClingObserver.ExecuteCallback callback) {
        if (device == null) {
            executeError("No screen projection device selected\n", callback);
            return null;
        }
        final Service service = device.findService(AV_TRANSPORT_SERVICE);
        if (service == null) {
            executeError("Receiving device service abnormal\n", callback);
            return null;
        }
        return service;
    }

    /**
     * Get the equipment service of the projection screen
     */
    @Nullable
    private Service getControlService(@Nullable Device device, @Nullable DlnaClingObserver.ExecuteCallback callback) {
        if (device == null) {
            executeError("No screen projection device selected\n", callback);
            return null;
        }
        final Service service = device.findService(RENDERING_CONTROL_SERVICE);
        if (service == null) {
            executeError("Receiving device service abnormal\n", callback);
            return null;
        }
        return service;
    }

    /**
     * Get transfer service
     */
    @Nullable
    public Service getTransportService(@NotNull Device device) {
        return device.findService(AV_TRANSPORT_SERVICE);
    }

    /**
     * Get controlled service
     */
    @Nullable
    public Service getControlService(@NotNull Device device) {
        return device.findService(RENDERING_CONTROL_SERVICE);
    }

    /**
     * Execution exception
     */
    private void executeError(String msg, @Nullable DlnaClingObserver.ExecuteCallback callback) {
        if (callback != null)
            callback.failure(null, new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED), msg);
    }

    public void setUpnpService(@Nullable AndroidUpnpService upnpService) {
        this.mUpnpService = upnpService;
    }

    @Nullable
    public AndroidUpnpService getUpnpService() {
        return mUpnpService;
    }
}
