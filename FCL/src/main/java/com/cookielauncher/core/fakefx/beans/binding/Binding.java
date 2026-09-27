package com.cookielauncher.core.fakefx.beans.binding;

import com.cookielauncher.core.fakefx.beans.value.ObservableValue;
import com.cookielauncher.core.fakefx.collections.ObservableList;

public interface Binding<T> extends ObservableValue<T> {

    boolean isValid();

    void invalidate();

    ObservableList<?> getDependencies();

    void dispose();

}
