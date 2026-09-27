package com.cookielauncher.app.ui.account;

import android.content.Context;

import androidx.annotation.NonNull;

import com.cookielauncher.core.auth.AuthInfo;
import com.cookielauncher.core.auth.ClassicAccount;
import com.cookielauncher.library.component.dialog.FCLDialog;

import java.util.function.Consumer;

public class ClassicAccountLoginDialog extends FCLDialog {
    public ClassicAccountLoginDialog(@NonNull Context context, ClassicAccount oldAccount, Consumer<AuthInfo> success, Runnable failed) {
        super(context);
    }
}
