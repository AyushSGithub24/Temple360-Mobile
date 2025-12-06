package com.imengyu.vr720;

import android.app.Application;
import android.util.Log;

import com.imengyu.vr720.utils.ToastUtils;
import com.imengyu.vr720.core.natives.NativeVR720;
import com.imengyu.vr720.service.CacheServices;
import com.imengyu.vr720.service.ErrorUploadService;
import com.imengyu.vr720.service.ListDataService;
import com.imengyu.vr720.service.ListImageCacheService;
import com.imengyu.vr720.service.UpdateService;
import com.imengyu.vr720.utils.CrashHandler;

/**
 * Application global initialization operations
 */
public class VR720Application extends Application {

    public static final String TAG = VR720Application.class.getSimpleName();

    // 🔥 Add static singleton instance
    private static VR720Application instance;

    public static VR720Application getInstance() {
        return instance;
    }

    //Global service use
    //===================================

    private ListDataService listDataService = null;
    private ListImageCacheService listImageCacheService = null;
    private CacheServices cacheServices = null;
    private UpdateService updateService = null;
    private ErrorUploadService errorUploadService = null;

    public ListDataService getListDataService() {
        return listDataService;
    }
    public ListImageCacheService getListImageCacheService() {
        return listImageCacheService;
    }
    public CacheServices getCacheServices() {
        return cacheServices;
    }
    public UpdateService getUpdateService() {
        if(updateService == null)
            updateService = new UpdateService();
        return updateService;
    }
    public ErrorUploadService getErrorUploadService() {
        if(errorUploadService == null)
            errorUploadService = new ErrorUploadService();
        return errorUploadService;
    }

    //Global initialization method
    //===================================

    private boolean initFinish = false;

    public boolean isNotInit() { return !initFinish; }
    public void setInitFinish() { initFinish = true; }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        // 🔥 Initialize the singleton instance
        instance = this;

        //Set error callback
        CrashHandler.getInstance().init(getApplicationContext(), false);

        //Initialize data service
        listDataService = new ListDataService(getApplicationContext());
        listImageCacheService = new ListImageCacheService(getApplicationContext());
        cacheServices = new CacheServices(getApplicationContext());

        //ToastUtils
//        ToastUtils.init(this);

        //Initialize the kernel
        NativeVR720.startLoadLibrary();
        NativeVR720.initNative(getAssets(), getApplicationContext());

    }

    @Override
    public void onTerminate() {
        Log.i(TAG, "onTerminate");
        if(listImageCacheService != null) {
            listImageCacheService.releaseImageCache();
            listImageCacheService = null;
        }
        if(NativeVR720.isLibLoadSuccess())
            NativeVR720.releaseNative();
        initFinish = false;
        super.onTerminate();
    }

    public void onQuit() {
        Log.i(TAG, "onQuit");
        if(NativeVR720.isLibLoadSuccess())
            NativeVR720.releaseNative();
        initFinish = false;
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory");
        if(listImageCacheService != null)
            listImageCacheService.releaseAllMemoryCache();
        if(NativeVR720.isLibLoadSuccess())
            NativeVR720.lowMemory();
        super.onLowMemory();
    }
}
