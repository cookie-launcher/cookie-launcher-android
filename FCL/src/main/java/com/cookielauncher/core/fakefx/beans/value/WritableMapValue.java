package com.cookielauncher.core.fakefx.beans.value;

import com.cookielauncher.core.fakefx.collections.ObservableMap;

public interface WritableMapValue<K, V> extends WritableObjectValue<ObservableMap<K,V>>, ObservableMap<K, V> {
}
