package com.cookielauncher.app.ui.download.modpack;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ScrollView;

import com.cookielauncher.app.R;
import com.cookielauncher.app.setting.Profile;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.library.component.dialog.FCLAlertDialog;
import com.cookielauncher.library.component.theme.ThemeEngine;
import com.cookielauncher.library.component.ui.FCLPage;
import com.cookielauncher.library.component.view.FCLButton;
import com.cookielauncher.library.component.view.FCLEditText;
import com.cookielauncher.library.component.view.FCLLinearLayout;
import com.cookielauncher.library.component.view.FCLProgressBar;
import com.cookielauncher.library.component.view.FCLTextView;

public abstract class ModpackPage extends FCLPage implements View.OnClickListener {

    protected final Profile profile;

    protected FCLProgressBar progressBar;
    protected FCLLinearLayout layout;
    protected ScrollView infoLayout;

    protected FCLEditText editText;
    protected FCLTextView name;
    protected FCLTextView version;
    protected FCLTextView author;

    protected FCLButton install;
    protected FCLButton describe;

    public ModpackPage(Context context, int id, int resId, Profile profile) {
        super(context, id, resId);
        this.profile = profile;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        progressBar = findViewById(R.id.progress);
        layout = findViewById(R.id.layout);
        infoLayout = findViewById(R.id.info_layout);
        editText = findViewById(R.id.name);
        name = findViewById(R.id.modpack_name);
        version = findViewById(R.id.version);
        author = findViewById(R.id.author);
        install = findViewById(R.id.install);
        describe = findViewById(R.id.describe);
        install.setOnClickListener(this);
        describe.setOnClickListener(this);
        ThemeEngine.getInstance().registerEvent(infoLayout, () -> infoLayout.setBackgroundTintList(new ColorStateList(new int[][]{{}}, new int[]{ThemeEngine.getInstance().getTheme().getLtColor()})));
    }

    @Override
    public Task<?> refresh(Object... param) {
        return null;
    }


    protected abstract void onInstall();

    protected abstract void onDescribe();

    @Override
    public void onClick(View v) {
        if (v == install) {
            FCLAlertDialog dialog = new FCLAlertDialog(getContext());
            dialog.setTitle(R.string.modpack_download_warn_title);
            dialog.setMessage(getContext().getString(R.string.modpack_download_warn_msg));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setPositiveButton(getContext().getString(com.cookielauncher.app.R.string.dialog_positive), this::onInstall);
            dialog.setNegativeButton(getContext().getString(com.cookielauncher.app.R.string.dialog_negative), null);
            dialog.show();
        }
        if (v == describe) {
            onDescribe();
        }
    }
}
