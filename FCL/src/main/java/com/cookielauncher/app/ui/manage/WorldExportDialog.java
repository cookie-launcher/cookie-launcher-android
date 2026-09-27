package com.cookielauncher.app.ui.manage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.cookielauncher.app.R;
import com.cookielauncher.app.ui.TaskDialog;
import com.cookielauncher.app.ui.UIManager;
import com.cookielauncher.app.util.TaskCancellationAction;
import com.cookielauncher.core.fakefx.beans.binding.Bindings;
import com.cookielauncher.core.game.World;
import com.cookielauncher.core.task.Schedulers;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.core.task.TaskExecutor;
import com.cookielauncher.core.task.TaskListener;
import com.cookielauncher.core.util.StringUtils;
import com.cookielauncher.core.util.platform.OperatingSystem;
import com.cookielauncher.library.component.dialog.FCLAlertDialog;
import com.cookielauncher.library.component.dialog.FCLDialog;
import com.cookielauncher.library.component.view.FCLButton;
import com.cookielauncher.library.component.view.FCLEditText;

import java.io.File;
import java.nio.file.Paths;

public class WorldExportDialog extends FCLDialog implements View.OnClickListener {

    private final World world;
    private final String parent;

    private FCLEditText editFileName;
    private FCLEditText editName;
    private FCLButton positive;
    private FCLButton negative;

    @SuppressLint("SetTextI18n")
    public WorldExportDialog(@NonNull Context context, World world, String parent) {
        super(context);
        this.world = world;
        this.parent = parent;
        setCancelable(false);
        setContentView(R.layout.dialog_world_export);

        editFileName = findViewById(R.id.file_name);
        editName = findViewById(R.id.name);
        editFileName.setStringValue(world.getWorldName() + ".zip");
        editName.setStringValue(world.getWorldName());
        editFileName.setText(world.getWorldName() + ".zip");
        editName.setText(world.getWorldName());
        positive = findViewById(R.id.positive);
        negative = findViewById(R.id.negative);
        positive.setOnClickListener(this);
        negative.setOnClickListener(this);

        positive.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        editName.getStringValue().isEmpty()
                                || StringUtils.isBlank(editFileName.getStringValue())
                                || !OperatingSystem.isNameValid(editFileName.getStringValue())
                                || new File(parent, editFileName.getStringValue()).exists(),
                editName.stringProperty().isEmpty(), editFileName.stringProperty()));
    }

    @Override
    public void onClick(View v) {
        if (v == positive) {
            TaskDialog taskDialog = new TaskDialog(getContext(), new TaskCancellationAction(AppCompatDialog::dismiss));
            taskDialog.setTitle(getContext().getString(R.string.message_doing));

            Task<?> task = Task.runAsync(getContext().getString(R.string.world_export_wizard, editName.getStringValue()), () -> world.export(Paths.get(new File(parent, editFileName.getText().toString()).getAbsolutePath()), editName.getStringValue()));
            TaskExecutor executor = task.executor(new TaskListener() {
                @Override
                public void onStop(boolean success, TaskExecutor executor) {
                    Schedulers.androidUIThread().execute(() -> {
                        if (success) {
                            FCLAlertDialog.Builder builder1 = new FCLAlertDialog.Builder(getContext());
                            builder1.setAlertLevel(FCLAlertDialog.AlertLevel.INFO);
                            builder1.setCancelable(false);
                            builder1.setMessage(getContext().getString(R.string.message_success));
                            builder1.setNegativeButton(getContext().getString(com.cookielauncher.app.R.string.dialog_positive), () -> UIManager.getInstance().getManageUI().dismissAllTempPages());
                            builder1.create().show();
                        } else {
                            if (executor.getException() == null)
                                return;
                            String appendix = StringUtils.getStackTrace(executor.getException());
                            FCLAlertDialog.Builder builder1 = new FCLAlertDialog.Builder(getContext());
                            builder1.setAlertLevel(FCLAlertDialog.AlertLevel.ALERT);
                            builder1.setCancelable(false);
                            builder1.setTitle(getContext().getString(R.string.message_failed));
                            builder1.setMessage(appendix);
                            builder1.setNegativeButton(getContext().getString(com.cookielauncher.app.R.string.dialog_positive), null);
                            builder1.create().show();
                        }
                    });
                }
            });
            taskDialog.setExecutor(executor);
            taskDialog.show();
            executor.start();
            dismiss();
        }
        if (v == negative) {
            dismiss();
        }
    }
}
