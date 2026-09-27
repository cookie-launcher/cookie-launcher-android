package com.cookielauncher.app.ui.version;

import android.content.Context;

import com.cookielauncher.app.R;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.library.component.ui.FCLMultiPageUI;
import com.cookielauncher.library.component.ui.FCLPage;
import com.cookielauncher.library.component.view.FCLUILayout;

public class VersionUI extends FCLMultiPageUI {

    public static final int PAGE_ID_VERSION_LIST = 15020;

    private FCLUILayout container;

    public VersionUI(Context context, int id) {
        super(context, id);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        container = findViewById(R.id.container);
        setupPages(container, null);
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public FCLPage createPage(int position) {
        return new VersionListPage(getContext(), PAGE_ID_VERSION_LIST);
    }

    @Override
    public Task<?> refresh(Object... param) {
        return Task.runAsync(() -> {

        });
    }
}
