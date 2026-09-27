package com.cookielauncher.app.control;

import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.mio.ui.view.CursorView;
import com.cookielauncher.bridge.bridge.FCLBridge;
import com.cookielauncher.bridge.bridge.FCLBridgeCallback;
import com.cookielauncher.library.component.FCLActivity;
import com.cookielauncher.library.component.view.FCLImageView;

public interface MenuCallback {

    void setup(FCLActivity activity, FCLBridge fclBridge);

    View getLayout();

    @Nullable
    FCLBridge getBridge();

    FCLBridgeCallback getCallbackBridge();

    FCLInput getInput();

    CursorView getCursor();

    int getCursorMode();

    void onPause();

    void onResume();

    void onGraphicOutput();

    void onCursorModeChange(int mode);

    void onLog(String log);

    void onExit(int exitCode);

}
