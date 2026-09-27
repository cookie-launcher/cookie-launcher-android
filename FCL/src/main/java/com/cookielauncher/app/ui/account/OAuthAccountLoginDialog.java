package com.cookielauncher.app.ui.account;

import static com.cookielauncher.core.util.Logging.LOG;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.cookielauncher.app.R;
import com.cookielauncher.app.game.OAuthServer;
import com.cookielauncher.app.setting.Accounts;
import com.mio.util.AndroidUtilKt;
import com.mio.util.LoginStageTextBinder;
import com.cookielauncher.app.util.FXUtils;
import com.cookielauncher.core.auth.AuthInfo;
import com.cookielauncher.core.auth.OAuthAccount;
import com.cookielauncher.core.auth.microsoft.MicrosoftAccount;
import com.cookielauncher.core.fakefx.beans.property.ObjectProperty;
import com.cookielauncher.core.fakefx.beans.property.SimpleObjectProperty;
import com.cookielauncher.core.task.Schedulers;
import com.cookielauncher.core.task.Task;
import com.cookielauncher.library.component.dialog.FCLAlertDialog;
import com.cookielauncher.library.component.dialog.FCLDialog;
import com.cookielauncher.library.component.view.FCLButton;

import android.widget.TextView;

import java.util.function.Consumer;
import java.util.logging.Level;

public class OAuthAccountLoginDialog extends FCLDialog implements View.OnClickListener {

    private final FCLButton positive;
    private final FCLButton negative;

    /** 登录进度行（include view_login_progress），微软重登时显示当前阶段 */
    private final View loginProgress;
    private final TextView progressText;

    private final OAuthAccount account;
    private final Consumer<AuthInfo> success;
    private final Runnable failed;
    private final ObjectProperty<OAuthServer.GrantDeviceCodeEvent> deviceCode = new SimpleObjectProperty<>();

    // 强引用注册（register），dismiss 时注销，避免对话框关闭后残留监听重复打开登录页
    private final Consumer<OAuthServer.GrantDeviceCodeEvent> deviceCodeListener = deviceCode::set;
    private boolean useExternalBrowser = false;
    private final Consumer<OAuthServer.OpenBrowserEvent> openBrowserListener = event -> {
        if (useExternalBrowser) {
            AndroidUtilKt.openLink(getContext(), event.getUrl());
        } else {
            AndroidUtilKt.openLinkWithBuiltinWebView(getContext(), event.getUrl());
        }
    };

    public OAuthAccountLoginDialog(@NonNull Context context, OAuthAccount account, Consumer<AuthInfo> success, Runnable failed) {
        super(context);
        this.account = account;
        this.success = success;
        this.failed = failed;

        setContentView(R.layout.dialog_relogin_oauth);
        setCancelable(false);

        FXUtils.onChangeAndOperate(deviceCode, deviceCode -> Schedulers.androidUIThread().execute(() -> {
            if (deviceCode != null) {
                AndroidUtilKt.copyText(getContext(), deviceCode.getUserCode());
            }
        }));
        Accounts.OAUTH_CALLBACK.onGrantDeviceCode.register(deviceCodeListener);
        Accounts.OAUTH_CALLBACK.onOpenBrowser.register(openBrowserListener);

        positive = findViewById(R.id.login);
        negative = findViewById(R.id.cancel);
        loginProgress = findViewById(R.id.login_progress);
        progressText = findViewById(R.id.progress_text);

        positive.setOnClickListener(this);
        negative.setOnClickListener(this);

        positive.setOnLongClickListener(view -> {
            useExternalBrowser = true;
            onClick(positive);
            return true;
        });
    }

    @Override
    public void dismiss() {
        Accounts.OAUTH_CALLBACK.onGrantDeviceCode.unregister(deviceCodeListener);
        Accounts.OAUTH_CALLBACK.onOpenBrowser.unregister(openBrowserListener);
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (view == positive) {
            positive.setEnabled(false);
            negative.setEnabled(false);
            // 微软账户注入登录进度回调，实时显示各认证阶段
            MicrosoftAccount microsoftAccount = account instanceof MicrosoftAccount ? (MicrosoftAccount) account : null;
            if (microsoftAccount != null) {
                loginProgress.setVisibility(View.VISIBLE);
                progressText.setText(R.string.launch_state_logging_in);
                microsoftAccount.setProgressCallback(new LoginStageTextBinder(getContext(), progressText));
            }
            Task.supplyAsync(account::logInWhenCredentialsExpired)
                    .whenComplete(Schedulers.androidUIThread(), (authInfo, exception) -> {
                        if (microsoftAccount != null) {
                            microsoftAccount.setProgressCallback(null);
                            loginProgress.setVisibility(View.GONE);
                        }
                        if (exception == null) {
                            success.accept(authInfo);
                            dismiss();
                        } else {
                            LOG.log(Level.INFO, "Failed to login when credentials expired: " + account, exception);
                            FCLAlertDialog.Builder builder = new FCLAlertDialog.Builder(getContext());
                            builder.setAlertLevel(FCLAlertDialog.AlertLevel.ALERT);
                            builder.setMessage(Accounts.localizeErrorMessage(getContext(), exception));
                            builder.setCancelable(false);
                            builder.setNegativeButton(getContext().getString(com.cookielauncher.app.R.string.dialog_positive), null);
                            builder.create().show();
                        }
                        positive.setEnabled(true);
                        negative.setEnabled(true);
                        // 登录流程已终结（成功/失败），通知内嵌登录页自行关闭
                        Accounts.OAUTH_CALLBACK.onLoginFinished.fireEvent(new OAuthServer.LoginFinishedEvent(this));
                    }).start();
        }
        if (view == negative) {
            Accounts.OAUTH_CALLBACK.onLoginFinished.fireEvent(new OAuthServer.LoginFinishedEvent(this));
            failed.run();
            dismiss();
        }
    }
}
