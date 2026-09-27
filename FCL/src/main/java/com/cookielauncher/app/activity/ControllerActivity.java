package com.cookielauncher.app.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.cookielauncher.app.control.GameMenu;
import com.cookielauncher.app.control.view.MenuView;
import com.cookielauncher.library.component.FCLActivity;
import com.cookielauncher.library.component.theme.ThemeEngine;
import com.cookielauncher.library.component.view.FCLImageView;

public class ControllerActivity extends FCLActivity {

    private GameMenu menu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FCLImageView contentView = new FCLImageView(this);
        contentView.setBackground(ThemeEngine.getInstance().getTheme().getBackground(this));
        setContentView(contentView);

        menu = new GameMenu();
        menu.setup(this, null);

        addContentView(menu.getLayout(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)) {
            DrawerLayout drawerLayout = (DrawerLayout) menu.getLayout();
            drawerLayout.openDrawer(GravityCompat.START, true);
            drawerLayout.openDrawer(GravityCompat.END, true);
        }
        return super.dispatchKeyEvent(event);
    }
}
