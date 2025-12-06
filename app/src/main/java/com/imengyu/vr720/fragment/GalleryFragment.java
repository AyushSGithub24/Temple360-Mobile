package com.imengyu.vr720.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.imengyu.vr720.utils.ToastUtils;

import com.imengyu.vr720.activity.GalleryActivity;
import com.imengyu.vr720.activity.MainActivity;
import com.imengyu.vr720.R;
import com.imengyu.vr720.VR720Application;
import com.imengyu.vr720.config.Codes;
import com.imengyu.vr720.config.MainMessages;
import com.imengyu.vr720.dialog.CommonDialog;
import com.imengyu.vr720.dialog.fragment.ChooseItemDialogFragment;
import com.imengyu.vr720.list.GalleryList;
import com.imengyu.vr720.model.GalleryItem;
import com.imengyu.vr720.model.TitleSelectionChangedCallback;
import com.imengyu.vr720.model.list.GalleryListItem;
import com.imengyu.vr720.service.ListDataService;
import com.imengyu.vr720.widget.MyTitleBar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GalleryFragment extends Fragment implements IMainFragment {

    public GalleryFragment() {}

    private TitleSelectionChangedCallback titleSelectionChangedCallback;
    private ListDataService listDataService;
    private Handler handler;
    private MyTitleBar titleBar;

    private GalleryList galleryList = null;
    private RefreshLayout refreshLayout = null;

    // ---------------------------------------------------------
    // REQUIRED INTERFACE METHODS (FIXED)
    // ---------------------------------------------------------

    @Override
    public void setTitleSelectionChangedCallback(TitleSelectionChangedCallback callback) {
        titleSelectionChangedCallback = callback;
    }

    @Override
    public void setTitleSelectionCheckAllSwitch() {
        if (galleryList != null) {
            if (galleryList.getSelectedItemCount() >= galleryList.getCheckableItemsCount())
                galleryList.clearSelectedItems();
            else
                galleryList.selectAllItems();
        }
    }

    @Override
    public void setTitleSelectionQuit() {
        if (galleryList != null)
            galleryList.setListCheckMode(false);
    }

    @Override
    public boolean onBackPressed() {
        if (galleryList != null && galleryList.isListCheckMode()) {
            galleryList.setListCheckMode(false);
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------
    // FRAGMENT LIFECYCLE
    // ---------------------------------------------------------

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.layout_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            listDataService = mainActivity.getListDataService();
            handler = mainActivity.getHandler();
            titleBar = mainActivity.getToolbar();
        }

        initMenu();
        initView(view);
        loadSettings();
        loadList();
    }

    // ---------------------------------------------------------
    // INITIALIZATIONS
    // ---------------------------------------------------------

    private void initView(View view) {

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> onAddGalleryClick());

        LinearLayout footerSelection = view.findViewById(R.id.footer_select);
        footerSelection.setVisibility(View.GONE);

        ListView listView = view.findViewById(R.id.list_gallery);

        View button_rename = view.findViewById(R.id.button_rename);
        View button_delete = view.findViewById(R.id.button_delete);
        View button_selection_more = view.findViewById(R.id.button_selection_more);

        button_rename.setOnClickListener(v -> onRenameGalleryClick());
        button_delete.setOnClickListener(v -> onDeleteGalleryClick());
        button_selection_more.setOnClickListener(v -> onGalleryMoreClick());

        galleryList = new GalleryList(
                getActivity(),
                requireContext(),
                ((VR720Application) requireActivity().getApplication()).getListImageCacheService());

        galleryList.init(handler, listView);

        listView.setDividerHeight(0);
        listView.setDivider(null);
        listView.setEmptyView(view.findViewById(R.id.empty_main));

        refreshLayout = view.findViewById(R.id.refreshLayout);
        if (refreshLayout != null)
            refreshLayout.setEnableRefresh(false);
    }

    private PopupMenu mainMenu;
    private MenuItem action_sort_date;
    private MenuItem action_sort_name;

    private void initMenu() {

        mainMenu = new PopupMenu(
                getActivity(),
                titleBar == null ? null : titleBar.getRightButton(),
                Gravity.TOP
        );

        Menu menu = mainMenu.getMenu();
        mainMenu.getMenuInflater().inflate(R.menu.menu_gallerys, menu);
        mainMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);

        action_sort_date = menu.findItem(R.id.action_sort_date);
        action_sort_name = menu.findItem(R.id.action_sort_name);
    }

    @Override
    public void showMore() {
        if (mainMenu != null)
            mainMenu.show();
    }

    // ---------------------------------------------------------
    // LOADING LIST
    // ---------------------------------------------------------

    private void loadList() {

        galleryList.clear();

        ArrayList<GalleryItem> items = listDataService.getGalleryList();

        for (GalleryItem item : items)
            galleryList.addItem(new GalleryListItem(item), false);

        galleryList.notifyChange();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (handler != null)
                    handler.sendEmptyMessage(MainMessages.MSG_REFRESH_GALLERY_LIST);
            }
        }, 300);
    }

    SharedPreferences sharedPreferences;

    private void loadSettings() {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        galleryList.setSortReverse(sharedPreferences.getBoolean("gallery_list_sort_reverse", false));
        galleryList.setSortType(sharedPreferences.getInt("gallery_list_sort_type",
                GalleryList.GALLERY_SORT_NAME));

        galleryList.sort();
        updateSortMenuActive();
    }

    private void saveSettings() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putInt("gallery_list_sort_type", galleryList.getSortType());
        e.putBoolean("gallery_list_sort_reverse", galleryList.isSortReverse());
        e.apply();
    }

    @Override
    public void handleMessage(Message msg) {

        switch (msg.what) {

            case MainMessages.MSG_LIST_LOAD_FINISH:
                if (refreshLayout != null)
                    refreshLayout.finishRefresh();
                break;

            case MainMessages.MSG_REFRESH_GALLERY_LIST:
                galleryList.refresh();
                break;

            case MainMessages.MSG_FORCE_LOAD_LIST:
                loadList();
                break;
        }
    }

    @Override
    public void onPause() {
        saveSettings();
        super.onPause();
    }

    // ---------------------------------------------------------
    // ACTION HANDLERS
    // ---------------------------------------------------------

    private void onAddGalleryClick() {

        new CommonDialog(requireActivity())
                .setEditTextHint(R.string.text_enter_gallery_name)
                .setTitle(R.string.action_new_gallery)
                .setPositiveEnable(false)
                .setCancelable(true)
                .setOnEditTextChangedListener((txt, dlg) ->
                        dlg.setPositiveEnable(txt.length() > 0))
                .setPositive(R.string.action_ok)
                .setNegative(R.string.action_cancel)
                .setOnResult((ok, dlg) -> {

                    if (ok == CommonDialog.BUTTON_POSITIVE) {

                        GalleryListItem item = new GalleryListItem();
                        item.setId(listDataService.getGalleryListMinId());
                        item.setName(dlg.getEditText().getText().toString());

                        galleryList.addItem(item, true);
                        listDataService.addGalleryItem(item.toGalleryItem());
                        return true;
                    }
                    return false;
                })
                .show();
    }

    private void onDeleteGalleryClick() {

        List<GalleryListItem> sel = galleryList.getSelectedItems();
        if (sel.isEmpty()) return;

        new CommonDialog(requireActivity())
                .setTitle(R.string.text_sure_delete_gallery)
                .setMessage(R.string.text_gallery_list_will_be_clear)
                .setPositive(R.string.action_sure_delete)
                .setNegative(R.string.action_cancel)
                .setOnResult((b, dlg) -> {

                    if (b == CommonDialog.BUTTON_POSITIVE) {
                        for (GalleryListItem g : sel)
                            listDataService.removeGalleryItem(g.getId());

                        galleryList.deleteItems(sel);
                        return true;
                    }
                    return false;
                })
                .show();
    }

    private void onRenameGalleryClick() {

        List<GalleryListItem> sel = galleryList.getSelectedItems();
        if (sel.isEmpty()) return;

        GalleryListItem item = sel.get(0);

        new CommonDialog(requireActivity())
                .setEditTextHint(R.string.text_enter_gallery_name)
                .setEditTextValue(item.getName())
                .setTitle(R.string.text_rename_gallery)
                .setPositiveEnable(true)
                .setCancelable(true)
                .setOnEditTextChangedListener((txt, dlg) ->
                        dlg.setPositiveEnable(txt.length() > 0))
                .setPositive(R.string.action_ok)
                .setNegative(R.string.action_cancel)
                .setOnResult((b, dlg) -> {

                    if (b == CommonDialog.BUTTON_POSITIVE) {

                        String newName = dlg.getEditTextValue().toString();
                        item.setName(newName);
                        listDataService.renameGalleryItem(item.getId(), newName);
                        galleryList.notifyChange();
                        return true;
                    }
                    return false;
                })
                .show();
    }

    private void onGalleryMoreClick() {

        List<GalleryListItem> sel = galleryList.getSelectedItems();
        if (sel.isEmpty()) return;

        new ChooseItemDialogFragment(null,
                new String[]{
                        getString(R.string.text_show_in_main),
                        getString(R.string.text_do_not_show_in_main)
                })
                .setOnChooseItemListener((ok, index, obj) -> {

                    if (!ok) return;

                    for (GalleryListItem g : sel)
                        listDataService.setGalleryListItemShowInMain(
                                g.getId(),
                                index == 0);

                    listDataService.setDataDirty(true);
                    handler.sendEmptyMessageDelayed(MainMessages.MSG_FORCE_LOAD_LIST, 1000);

                    ToastUtils.show(getContext(),getString(
                            index == 0
                                    ? R.string.text_select_items_showed_in_main
                                    : R.string.text_select_items_hidden_in_main
                    ));

                    galleryList.setListCheckMode(false);
                })
                .show(getParentFragmentManager(), "GalleryMore");
    }

    private void onOpenGalleryClick(GalleryListItem item) {
        Intent i = new Intent(getActivity(), GalleryActivity.class);
        i.putExtra("galleryId", item.getId());
        startActivityForResult(i, Codes.REQUEST_CODE_GALLERY);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_new_gallery) {
            onAddGalleryClick();
        } else if (item.getItemId() == R.id.action_sort_date) {
            galleryList.sort(GalleryList.GALLERY_SORT_DATE);
            updateSortMenuActive();
        } else if (item.getItemId() == R.id.action_sort_name) {
            galleryList.sort(GalleryList.GALLERY_SORT_NAME);
            updateSortMenuActive();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateSortMenuActive() {

        int sort = galleryList.getSortType();
        int icon = galleryList.isSortReverse()
                ? R.drawable.ic_sort_up
                : R.drawable.ic_sort_down;

        action_sort_date.setIcon(R.drawable.ic_sort_none);
        action_sort_name.setIcon(R.drawable.ic_sort_none);

        if (sort == GalleryList.GALLERY_SORT_DATE)
            action_sort_date.setIcon(icon);
        else if (sort == GalleryList.GALLERY_SORT_NAME)
            action_sort_name.setIcon(icon);
    }
}
