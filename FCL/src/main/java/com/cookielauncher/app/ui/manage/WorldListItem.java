package com.cookielauncher.app.ui.manage;

import static com.cookielauncher.core.util.StringUtils.parseColorEscapes;
import com.cookielauncher.library.component.ui.FCLPage;
import com.cookielauncher.app.ui.UIManager;
import static com.cookielauncher.library.util.LocaleUtils.formatDateTime;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import com.cookielauncher.app.R;
import com.cookielauncher.app.activity.MainActivity;
import com.mio.util.DialogUtilKt;
import com.cookielauncher.core.fakefx.beans.property.SimpleStringProperty;
import com.cookielauncher.core.fakefx.beans.property.StringProperty;
import com.cookielauncher.core.game.World;
import com.cookielauncher.core.game.WorldLockedException;
import com.cookielauncher.core.task.Schedulers;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.library.component.dialog.EditDialog;
import com.cookielauncher.library.component.dialog.FCLAlertDialog;

import java.nio.file.Files;
import java.time.Instant;

public class WorldListItem {
    private final Context context;
    private final Activity activity;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subtitle = new SimpleStringProperty();
    private final World world;
    private final Bitmap icon;

    public WorldListItem(Context context, Activity activity, World world) {
        this.context = context;

        this.activity = activity;


        this.world = world;
        this.icon = world.getIcon();

        title.set(parseColorEscapes(world.getWorldName()));

        subtitle.set(context.getString(R.string.world_description, world.getFileName(), formatDateTime(context, Instant.ofEpochMilli(world.getLastPlayed())), world.getGameVersion() == null ? context.getString(R.string.message_unknown) : world.getGameVersion().toNormalizedString()));
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty subtitleProperty() {
        return subtitle;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void export() {
        MainActivity.getInstance().fileLauncher.launchSingleSelection(null, null, true, files -> {
            if (files == null) return;
            WorldExportDialog dialog = new WorldExportDialog(context, world, files.get(0).getPath());
            dialog.show();
        });
    }

    public void manageDatapacks() {
        if (!world.supportDataPacks()) {
            FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setAlertLevel(FCLAlertDialog.AlertLevel.INFO);
            builder.setMessage(context.getString(R.string.world_datapack_1_13));
            builder.setNegativeButton(context.getString(com.cookielauncher.app.R.string.dialog_positive), null);
            builder.create().show();
            return;
        }
        DatapackListPage page = new DatapackListPage(context, FCLPage.PAGE_ID_TEMP, world.getWorldName(), world.getFile());
        UIManager.getInstance().getManageUI().showTempPage(page);
    }

    public void showInfo() {
        try {
            WorldInfoPage page = new WorldInfoPage(context, FCLPage.PAGE_ID_TEMP, world);
            UIManager.getInstance().getManageUI().showTempPage(page);
        } catch (Exception e) {
            // TODO
        }
    }

    public void delete() {
        new FCLAlertDialog.Builder(context)
                .setMessage(context.getString(R.string.version_manage_remove_confirm, world.getWorldName()))
                .setPositiveButton(() -> Task.runAsync(Schedulers.io(), () -> world.delete())
                        .whenComplete(Schedulers.androidUIThread(), exception -> {
                            if (exception == null) {
                                notifyChanged();
                            } else if (exception instanceof WorldLockedException) {
                                DialogUtilKt.showErrorDialog(context, context.getString(R.string.world_locked_failed));
                            } else {
                                DialogUtilKt.showErrorDialog(context, exception.toString());
                            }
                        }).start())
                .setNegativeButton(null)
                .create()
                .show();
    }

    public void copy() {
        EditDialog dialog = new EditDialog(context, world.getWorldName(), name -> {
            if (Files.exists(world.getFile().resolveSibling(name))) {
                DialogUtilKt.showErrorDialog(context, context.getString(R.string.world_import_already_exists));
                return;
            }
            Task.runAsync(Schedulers.io(), () -> world.copy(name))
                    .whenComplete(Schedulers.androidUIThread(), exception -> {
                        if (exception == null) {
                            Toast.makeText(context, R.string.message_success, Toast.LENGTH_SHORT).show();
                            notifyChanged();
                        } else if (exception instanceof WorldLockedException) {
                            DialogUtilKt.showErrorDialog(context, context.getString(R.string.world_locked_failed));
                        } else {
                            DialogUtilKt.showErrorDialog(context, exception.toString());
                        }
                    }).start();
        });
        dialog.setTitle(R.string.world_duplicate);
        dialog.show();
    }

    private void notifyChanged() {
        WorldListPage page = (WorldListPage) UIManager.getInstance().getManageUI().getPage(4);
        page.refresh();
    }
}
