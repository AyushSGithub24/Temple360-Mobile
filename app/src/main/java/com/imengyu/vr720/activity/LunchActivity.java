package com.imengyu.vr720.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.imengyu.vr720.R;
import com.imengyu.vr720.VR720Application;
import com.imengyu.vr720.core.natives.NativeVR720;
import com.imengyu.vr720.dialog.CommonDialog;
import com.imengyu.vr720.utils.AppUtils;
import com.imengyu.vr720.utils.NotchScreenUtil;
import com.imengyu.vr720.utils.StatusBarUtils;
import com.imengyu.vr720.utils.StorageDirUtils;

public class LunchActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences = null;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set app language
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String language = sharedPreferences.getString("language", "");
        AppUtils.setLanguage(this, language);

        // Get intent parameters
        intent = getIntent();

        StatusBarUtils.setTransparentStatusBar(this);

        // Set splash view
        setContentView(R.layout.activity_lunch);

        // Check native libraries
        if(!runTestNative())
            return;

        // Continue immediately (permissions no longer required)
        AppUtils.testAgreementAllowed(this, (b) -> runContinue());
    }

    @Override
    public void onAttachedToWindow() {
        NotchScreenUtil.setCutoutForceShortEdges(this);
        super.onAttachedToWindow();
    }

    // ---------- Native check ----------
    private boolean runTestNative() {
        if(NativeVR720.isLibLoadSuccess())
            return true;

        new CommonDialog(this)
                .setTitle(R.string.text_app_failure)
                .setMessage(R.string.text_app_native_load_error)
                .setImageResource(R.drawable.ic_warning)
                .setOnResult((button, dialog) -> {
                    if(button == CommonDialog.BUTTON_POSITIVE) {
                        finish();
                        return true;
                    }
                    return false;
                })
                .show();

        return false;
    }

    // ---------- Continue to Main ----------
    private void runContinue() {
        new Thread(() -> {
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}

            // Create app directories (internal only)
            StorageDirUtils.testAndCreateStorageDirs(getApplicationContext());

            if(sharedPreferences.getBoolean("is_first_use", true)) {
                String path = AppUtils.writeDemoPanoramaToPictures(this);
                if(path != null) {
                    sharedPreferences.edit().putString("demo_panorama_path", path).apply();
                }
            }

            runOnUiThread(() -> {
                VR720Application.getInstance().setInitFinish();


                runMainActivity();
            });
        }).start();
    }

    private void runMainActivity() {
        Intent newIntent = new Intent(LunchActivity.this, MainActivity.class);

        // Forward any open/import parameters
        if(intent.hasExtra("openFilePath"))
            newIntent.putExtra("openFilePath", intent.getStringExtra("openFilePath"));
        if(intent.hasExtra("openFileArgPath"))
            newIntent.putExtra("openFileArgPath", intent.getStringExtra("openFileArgPath"));
        if(intent.hasExtra("openFileIsInCache"))
            newIntent.putExtra("openFileIsInCache", intent.getStringExtra("openFileIsInCache"));
        if(intent.hasExtra("importCount"))
            newIntent.putExtra("importCount", intent.getIntExtra("importCount", 0));
        if(intent.hasExtra("importList"))
            newIntent.putCharSequenceArrayListExtra("importList",
                    intent.getCharSequenceArrayListExtra("importList"));

        startActivity(newIntent);
        finish();
    }

    // ---------- Permissions Removed Completely ----------

    // App does not need external storage permission anymore.
    private boolean checkPermission() { return true; }

    private void showDialogTipUserRequestPermission() { runContinue(); }

    private void startRequestPermission() { runContinue(); }

    private void showDialogTipUserGoToAppSettings() { runContinue(); }

    private void goToAppSetting() { runContinue(); }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        runContinue();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
