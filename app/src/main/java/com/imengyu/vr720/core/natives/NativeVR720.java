package com.imengyu.vr720.core.natives;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class NativeVR720 {

    // Always report that native library is NOT needed
    public static boolean isLibLoadSuccess() {
        return true; // Pretend everything is OK
    }

    // Disable FFmpeg and native library loading completely
    public static boolean startLoadLibrary() {
        Log.i("NativeVR720", "Native loading disabled (FFmpeg removed).");
        return true;
    }

    // Disable all native functions (stub implementations)

    public static boolean initNative(AssetManager assetManager, Context context) {
        Log.i("NativeVR720", "initNative() skipped — native engine removed.");
        return true;
    }

    public static void releaseNative() {
        Log.i("NativeVR720", "releaseNative() skipped — no native engine loaded.");
    }

    public static void lowMemory() {
        Log.i("NativeVR720", "lowMemory() skipped — no native memory in use.");
    }

    public static String getNativeVersion() {
        return "Native engine disabled";
    }
}
