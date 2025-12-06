package com.imengyu.vr720.activity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.imengyu.vr720.R;
import com.imengyu.vr720.VR720Application;
import com.imengyu.vr720.adapter.MyFragmentAdapter;
import com.imengyu.vr720.config.Codes;
import com.imengyu.vr720.config.MainMessages;
import com.imengyu.vr720.dialog.CommonDialog;
import com.imengyu.vr720.dialog.LoadingDialog;
import com.imengyu.vr720.fragment.GalleryFragment;
import com.imengyu.vr720.fragment.HomeFragment;
import com.imengyu.vr720.fragment.IMainFragment;
import com.imengyu.vr720.model.TitleSelectionChangedCallback;
import com.imengyu.vr720.service.ErrorUploadService;
import com.imengyu.vr720.service.ListDataService;
import com.imengyu.vr720.service.UpdateService;
import com.imengyu.vr720.utils.AppPages;
import com.imengyu.vr720.utils.FileUtils;
import com.imengyu.vr720.utils.NetworkUtils;
import com.imengyu.vr720.utils.StatusBarUtils;
import com.imengyu.vr720.utils.StringUtils;
import com.imengyu.vr720.utils.ToastUtils;
import com.imengyu.vr720.widget.MyTitleBar;
import com.imengyu.vr720.widget.ToolbarButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // UI
    private Resources resources;
    private MyTitleBar toolbar;
    private DrawerLayout drawerLayout;
    private ViewPager2 mViewPager;

    // Fragments
    private final List<Fragment> fragments = new ArrayList<>();
    private HomeFragment homeFragment;
    private GalleryFragment galleryFragment;
    private IMainFragment currentFragment;
    private boolean currentTitleIsSelectMode = false;

    // Services
    private ListDataService listDataService;

    // Import dialog
    private LoadingDialog importLoadingDialog = null;

    // Handler / messages
    private final MainHandler handler = new MainHandler(this);

    // ActivityResultLaunchers for permissions and settings
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> settingsLauncher;

    // Pager adapter
    private FragmentStateAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resources = getResources();
        toolbar = findViewById(R.id.toolbar);

        StatusBarUtils.setLightMode(this);

        initActivityResultLaunchers();
//        requestAppropriateMediaPermissions();

        initDrawer();
        initList();
        initView();

        readParameters(getIntent());
        startChecks();
    }

    @Override
    protected void onDestroy() {
        ((VR720Application) getApplication()).onQuit();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (listDataService != null) listDataService.saveList();
        super.onPause();
    }

    // ---------- ActivityResult / Permission helpers ----------

    private void initActivityResultLaunchers() {

        // NO PERMISSION REQUEST NEEDED ANYMORE. We create a dummy launcher so code compiles.
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    // Do nothing — the app does not depend on these permissions anymore.
                }
        );

        // Settings launcher can remain but unused.
        settingsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // No need to re-check anything.
                }
        );
    }

    private void requestAppropriateMediaPermissions() {

    }

    private boolean hasAllMediaPermissions(String[] perms) {
        for (String p : perms) {
            if (checkSelfPermission(p) != android.content.pm.PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            settingsLauncher.launch(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            settingsLauncher.launch(intent);
        }
    }

    // ---------- UI init ----------

    private void initDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {
                View content = drawerLayout.getChildAt(0);
                int offset = (int) (drawerView.getWidth() * slideOffset);
                content.setTranslationX(offset);
            }
            @Override public void onDrawerOpened(@NonNull View drawerView) {}
            @Override public void onDrawerClosed(@NonNull View drawerView) {}
            @Override public void onDrawerStateChanged(int newState) {}
        });

        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setDrawerElevation(0f);
    }

    private void initView() {
        mViewPager = findViewById(R.id.view_pager_main);

        ToolbarButton buttonTabHome = findViewById(R.id.button_tab_home);
        ToolbarButton buttonTabGallery = findViewById(R.id.button_tab_gallery);

        buttonTabHome.setChecked(true);
        buttonTabHome.setOnClickListener((v) -> mViewPager.setCurrentItem(0, true));
        buttonTabGallery.setOnClickListener((v) -> mViewPager.setCurrentItem(1, true));

        homeFragment = new HomeFragment();
        galleryFragment = new GalleryFragment();

        TitleSelectionChangedCallback titleSelectionChangedCallback = (isSelectionMode, selCount, isAll) -> {
            currentTitleIsSelectMode = isSelectionMode;
            if (isSelectionMode) {
                DrawableCompat.setTint(toolbar.getRightButton().getForeground(),
                        isAll ? resources.getColor(R.color.colorPrimary, null) : Color.BLACK);

                toolbar.setTitle(selCount > 0 ?
                        String.format(getString(R.string.text_choosed_items), selCount) :
                        resources.getString(R.string.text_please_choose_item));

                toolbar.setLeftButtonIconResource(R.drawable.ic_close);
                toolbar.setRightButtonIconResource(R.drawable.ic_check_all);
                toolbar.setCustomViewsVisible(View.GONE);
            } else {
                DrawableCompat.setTint(toolbar.getRightButton().getForeground(), Color.BLACK);
                toolbar.setTitle("");
                toolbar.setLeftButtonIconResource(R.drawable.ic_menu);
                toolbar.setRightButtonIconResource(R.drawable.ic_more);
                toolbar.setCustomViewsVisible(View.VISIBLE);
            }
        };

        homeFragment.setTitleSelectionChangedCallback(titleSelectionChangedCallback);
        galleryFragment.setTitleSelectionChangedCallback(titleSelectionChangedCallback);

        fragments.clear();
        fragments.add(homeFragment);
        fragments.add(galleryFragment);

        currentFragment = homeFragment;

        pagerAdapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments.get(position);
            }

            @Override
            public int getItemCount() {
                return fragments.size();
            }
        };
        mViewPager.setAdapter(pagerAdapter);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                if (currentFragment != null) currentFragment.setTitleSelectionQuit();
                currentFragment = (IMainFragment) fragments.get(position);

                buttonTabHome.setChecked(position == 0);
                buttonTabGallery.setChecked(position == 1);
            }
        });

        ((ViewGroup)buttonTabHome.getParent()).removeView(buttonTabHome);
        ((ViewGroup)buttonTabGallery.getParent()).removeView(buttonTabGallery);
        toolbar.addCustomView(buttonTabHome);
        toolbar.addCustomView(buttonTabGallery);

        toolbar.setLeftIconOnClickListener((v) -> {
            if (currentTitleIsSelectMode) currentFragment.setTitleSelectionQuit();
            else if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START);
            else drawerLayout.openDrawer(GravityCompat.START);
        });

        toolbar.setRightIconOnClickListener((v) -> {
            if (currentTitleIsSelectMode) currentFragment.setTitleSelectionCheckAllSwitch();
            else currentFragment.showMore();
        });
    }

    // ---------- Data list ----------

    private void initList() {
        listDataService = ((VR720Application) getApplication()).getListDataService();
        listDataService.loadList();
    }

    // ---------- Parameters / import ----------

    private void readParameters(Intent intent) {
        if (intent == null) return;

        if (intent.hasExtra("openFilePath") && intent.hasExtra("openFileArgPath")
                && intent.hasExtra("openFileIsInCache")) {
            Intent newIntent = new Intent(this, PanoActivity.class);
            newIntent.putExtra("openFilePath", intent.getStringExtra("openFilePath"));
            newIntent.putExtra("openFileArgPath", intent.getStringExtra("openFileArgPath"));
            newIntent.putExtra("openFileIsInCache", intent.getBooleanExtra("openFileIsInCache", false));
            startActivityForResult(newIntent, Codes.REQUEST_CODE_PANO);
        } else if (intent.hasExtra("importCount") && intent.hasExtra("importList")) {
            importLoadingDialog = new LoadingDialog(this);
            importLoadingDialog.show();

            android.os.Message message = new android.os.Message();
            message.what = MainMessages.MSG_DO_LATE_IMPORT;
            message.obj = intent;
            handler.sendMessageDelayed(message, 1000);
        }
    }

    private void doImport(Intent intent) {
        if (homeFragment != null) {
            ArrayList<CharSequence> importList = intent.getCharSequenceArrayListExtra("importList");
            homeFragment.importFiles(importList, intent.getIntExtra("importCount", 0));
        }
        if (importLoadingDialog != null) {
            importLoadingDialog.dismiss();
            importLoadingDialog = null;
        }
    }

    // ---------- Back handling ----------

    private long exitTime = 0L;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }

            if (currentFragment != null && currentFragment.onBackPressed()) return true;

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.show(MainActivity.this ,resources.getText(R.string.text_press_once_more_to_quit));
                exitTime = System.currentTimeMillis();
            } else quit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void quit() {
        finish();
    }

    // ---------- Navigation ----------

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_import_file) handler.sendEmptyMessage(MainMessages.MSG_ADD_IMAGE);
        else if (id == R.id.nav_import_gallery) handler.sendEmptyMessage(MainMessages.MSG_ADD_IMAGE_GALLERY);
        else if (id == R.id.nav_manage) AppPages.showSettings(this);
        else if (id == R.id.nav_help) AppPages.showHelp(this);
        else if (id == R.id.nav_send) AppPages.showFeedBack(this);
        else if (id == R.id.nav_about) AppPages.showAbout(this);
        else if (id == R.id.nav_quit) quit();

        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Make listDataService accessible to fragments
    public ListDataService getListDataService() {
        return listDataService;
    }

    public MyTitleBar getToolbar() {
        return toolbar;
    }



    // ---------- Handler ----------

    private static class MainHandler extends android.os.Handler {
        private final WeakReference<MainActivity> mTarget;
        MainHandler(MainActivity target) {
            super(android.os.Looper.myLooper());
            mTarget = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(@NonNull android.os.Message msg) {
            MainActivity a = mTarget.get();
            if (a == null) return;

            if (msg.what == MainMessages.MSG_DO_LATE_IMPORT) {
                a.doImport((Intent) msg.obj);
            } else {
                for (Fragment fragment : a.fragments) {
                    ((IMainFragment) fragment).handleMessage(msg);
                }
            }
        }
    }

    public MainHandler getHandler() { return handler; }

    // ---------- onActivityResult ----------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Codes.REQUEST_CODE_SETTING && data != null
                && data.getBooleanExtra("needRestart", false)) {
            Intent intent = new Intent(this, LunchActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
            finish();
        }

        for (Fragment fragment : fragments) ((IMainFragment) fragment).onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    // ---------- Startup checks & updates ----------

    private void startChecks() {
        if (NetworkUtils.isNetworkConnected(this) && NetworkUtils.isNetworkWifi()) {
            checkUpdates();
            checkErrorLog();
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("is_first_use", true)) {
            String demoPath = sharedPreferences.getString("demo_panorama_path", "");
            if (!StringUtils.isNullOrEmpty(demoPath)) {
                android.os.Message message = new android.os.Message();
                message.what = MainMessages.MSG_IMPORT_DEMO;
                message.obj = demoPath;
                handler.sendMessageDelayed(message, 900);
            }
            sharedPreferences.edit().putBoolean("is_first_use", false).apply();
        }
    }

    // ---------- Update via system DownloadManager ----------

    private void downloadApk(String url) {
        try {
            android.app.DownloadManager.Request request =
                    new android.app.DownloadManager.Request(Uri.parse(url));
            request.setTitle(getString(R.string.app_name));
            request.setDescription(getString(R.string.text_downloading_update));
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "temple-update.apk");

            android.app.DownloadManager dm = (android.app.DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(request);

            ToastUtils.show(MainActivity.this,getString(R.string.text_downloading_update));
        } catch (Exception ex) {
            ToastUtils.show(MainActivity.this,getString(R.string.text_failed) + " " + ex.getMessage());
        }
    }

    private void checkUpdates() {
        if (!NetworkUtils.isNetworkConnected(this) || !NetworkUtils.isNetworkWifi()) return;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        long lastCheckTime = sharedPreferences.getLong("last_check_update_time", 0);
        if (new Date().getTime() - lastCheckTime < 86400000) return;
        sharedPreferences.edit().putLong("last_check_update_time", new Date().getTime()).apply();

        UpdateService updateService = ((VR720Application) getApplication()).getUpdateService();
        updateService.checkUpdate(new UpdateService.OnCheckUpdateCallback() {
            @Override
            public void onCheckUpdateSuccess(boolean hasUpdate, String newVer, int newVerCode, String newText, String md5, String downUrl) {
                if (hasUpdate) runOnUiThread(() -> downloadApk(downUrl));
            }

            @Override
            public void onCheckUpdateFailed(String err) {
                runOnUiThread(() -> ToastUtils.show(MainActivity.this,getString(R.string.text_update_failed) + " " + err));
            }
        });
    }

    // ---------- Error log ----------

    private void checkErrorLog() {
        if (!NetworkUtils.isNetworkConnected(this) || !NetworkUtils.isNetworkWifi()) return;

        ErrorUploadService errorUploadService = ((VR720Application) getApplication()).getErrorUploadService();
        errorUploadService.check(this, (hasError, path) -> {
            if (hasError) runOnUiThread(() -> askUserUploadErrorLog(path));
        });
    }

    private void askUserUploadErrorLog(String path) {
        new CommonDialog(this)
                .setMessage(R.string.text_a_report)
                .setMessage(String.format(getString(R.string.text_check_err), path))
                .setNegative(R.string.text_send_report)
                .setPositive(R.string.text_do_not_send_report)
                .setNeutral(R.string.text_i_want_check_report_content)
                .setOnResult((button, dialog) -> {
                    if (button == CommonDialog.BUTTON_POSITIVE) {
                        doUploadErrorLog();
                        return true;
                    } else if (button == CommonDialog.BUTTON_NEUTRAL) {
                        FileUtils.openFileWithApp(this, path);
                        return false;
                    }
                    return button == CommonDialog.BUTTON_NEGATIVE;
                })
                .show();
    }

    private void doUploadErrorLog() {
        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.show();
        ErrorUploadService errService = ((VR720Application) getApplication()).getErrorUploadService();
        errService.doUpload(this, (success, err) -> {
            loadingDialog.dismiss();
            if (success) ToastUtils.show(MainActivity.this,getString(R.string.text_feed_back_success));
            else ToastUtils.show(MainActivity.this,String.format(getString(R.string.text_feed_back_failed), err));
        });
    }
}
