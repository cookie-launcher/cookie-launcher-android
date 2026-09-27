package com.cookielauncher.app.ui.download.version;

import static com.cookielauncher.core.util.Logging.LOG;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cookielauncher.app.R;
import com.cookielauncher.app.setting.DownloadProviders;
import com.cookielauncher.app.ui.UIManager;
import com.cookielauncher.core.download.ComponentRemoteVersion;
import com.cookielauncher.core.download.ComponentVersionList;
import com.cookielauncher.core.game.GameComponentType;
import com.cookielauncher.core.task.Schedulers;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.core.util.versioning.GameVersionNumber;
import com.cookielauncher.library.component.ui.FCLPage;
import com.cookielauncher.library.component.view.FCLCheckBox;
import com.cookielauncher.library.component.view.FCLEditText;
import com.cookielauncher.library.component.view.FCLImageButton;
import com.cookielauncher.library.component.view.FCLProgressBar;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class VersionInstallPage extends FCLPage implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private FCLCheckBox checkRelease;
    private FCLCheckBox checkSnapShot;
    private FCLCheckBox checkOld;
    private FCLCheckBox checkAprilFools;
    private FCLImageButton refresh;
    private FCLImageButton failedRefresh;
    private FCLProgressBar progressBar;
    private RecyclerView recyclerView;
    private FCLEditText search;

    private RemoteVersionListAdapter.OnRemoteVersionSelectListener listener;

    public VersionInstallPage(Context context, int id) {
        super(context, id, R.layout.page_install_version);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkRelease = findViewById(R.id.release);
        checkSnapShot = findViewById(R.id.snapshot);
        checkOld = findViewById(R.id.old);
        checkAprilFools = findViewById(R.id.april_fools);
        refresh = findViewById(R.id.refresh);
        failedRefresh = findViewById(R.id.failed_refresh);
        progressBar = findViewById(R.id.progress);
        recyclerView = findViewById(R.id.list);
        search = findViewById(R.id.search);

        checkRelease.setChecked(true);

        checkRelease.setOnCheckedChangeListener(this);
        checkSnapShot.setOnCheckedChangeListener(this);
        checkOld.setOnCheckedChangeListener(this);
        checkAprilFools.setOnCheckedChangeListener(this);
        refresh.setOnClickListener(this);
        failedRefresh.setOnClickListener(this);

        listener = remoteVersion -> {
            VersionInstallInfoPage page = new VersionInstallInfoPage(getContext(), FCLPage.PAGE_ID_TEMP, remoteVersion.getGameVersion());
            UIManager.getInstance().getDownloadUI().showTempPage(page);
        };

        search.stringProperty().addListener(observable -> refreshDisplayVersions());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        refreshList();
    }

    private List<ComponentRemoteVersion> loadVersions() {
        return DownloadProviders.getDownloadProvider().getVersionList(GameComponentType.GAME).getVersions("").stream()
                .filter(it -> switch (it.getVersionType()) {
                    case RELEASE -> checkRelease.isChecked();
                    case PENDING, UNOBFUSCATED, SNAPSHOT -> {
                        if (checkSnapShot.isChecked()) yield true;
                        else if (checkAprilFools.isChecked())
                            yield GameVersionNumber.asGameVersion(it.getGameVersion()).isAprilFools();
                        yield false;
                    }
                    case OLD -> {
                        if (checkOld.isChecked()) yield true;
                        else if (checkAprilFools.isChecked())
                            yield GameVersionNumber.asGameVersion(it.getGameVersion()).isAprilFools();
                        yield false;
                    }
                    default -> true;
                })
                .filter(it -> it.getGameVersion().contains(search.getStringValue()))
                .sorted().collect(Collectors.toList());
    }

    public void refreshDisplayVersions() {
        List<ComponentRemoteVersion> items = loadVersions();
        RemoteVersionListAdapter adapter = new RemoteVersionListAdapter(getContext(), new ArrayList<>(items), listener);
        recyclerView.setAdapter(adapter);
    }

    public void refreshList() {
        recyclerView.setVisibility(View.GONE);
        failedRefresh.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        refresh.setEnabled(false);
        search.setText("");
        ComponentVersionList<?> currentVersionList = DownloadProviders.getDownloadProvider().getVersionList(GameComponentType.GAME);
        currentVersionList.refreshAsync("")
                .whenComplete(Schedulers.androidUIThread(), (result, exception) -> {
                    if (exception == null) {
                        List<ComponentRemoteVersion> items = loadVersions();

                        if (items.isEmpty()) {
                            checkRelease.setChecked(true);
                            checkSnapShot.setChecked(true);
                            checkOld.setChecked(true);
                        } else {
                            RemoteVersionListAdapter adapter = new RemoteVersionListAdapter(getContext(), new ArrayList<>(items), listener);
                            recyclerView.setAdapter(adapter);
                        }
                        recyclerView.setVisibility(View.VISIBLE);
                        failedRefresh.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        refresh.setEnabled(true);
                    } else {
                        LOG.log(Level.WARNING, "Failed to fetch versions list", exception);
                        recyclerView.setVisibility(View.GONE);
                        failedRefresh.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        refresh.setEnabled(true);
                    }

                    System.gc();
                }).start();
    }

    @Override
    public Task<?> refresh(Object... param) {
        return Task.runAsync(() -> {

        });
    }

    @Override
    public void onClick(View view) {
        if (view == refresh || view == failedRefresh) {
            refreshList();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton == checkRelease || compoundButton == checkSnapShot || compoundButton == checkOld || compoundButton == checkAprilFools) {
            refreshDisplayVersions();
        }
    }
}
