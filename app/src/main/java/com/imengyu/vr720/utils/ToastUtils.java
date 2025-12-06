package com.imengyu.vr720.utils;

import android.content.Context;
import android.widget.Toast;

public final class ToastUtils {

    private static Toast toast;

    // ALWAYS use application context (never activity/fragment context)
    public static void show(Context ctx, CharSequence text) {
        if (ctx == null) return;

        Context appCtx = ctx.getApplicationContext();
        if (toast != null) toast.cancel();

        toast = Toast.makeText(appCtx, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void show(Context ctx, int resId) {
        if (ctx == null) return;
        show(ctx, ctx.getString(resId));
    }
}
