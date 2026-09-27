package com.cookielauncher.app.ui.version;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cookielauncher.app.R;
import com.cookielauncher.library.component.dialog.FCLDialog;

public class ModpackSelectionDialog extends FCLDialog {

    public ModpackSelectionDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_modpack_selection);
        setCancelable(false);
    }
}
