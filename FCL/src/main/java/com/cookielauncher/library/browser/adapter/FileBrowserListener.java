package com.cookielauncher.library.browser.adapter;

public interface FileBrowserListener {
    void onEnterDir(String path);
    void onSelect(FileBrowserAdapter adapter, String path);
}
