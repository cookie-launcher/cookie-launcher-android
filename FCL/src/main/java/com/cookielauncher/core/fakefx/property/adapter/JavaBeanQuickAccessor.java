package com.cookielauncher.core.fakefx.property.adapter;

import com.cookielauncher.core.fakefx.beans.property.adapter.ReadOnlyJavaBeanObjectProperty;
import com.cookielauncher.core.fakefx.beans.property.adapter.ReadOnlyJavaBeanObjectPropertyBuilder;

public final class JavaBeanQuickAccessor {

    private JavaBeanQuickAccessor() {
    }

    public static <T> ReadOnlyJavaBeanObjectProperty<T> createReadOnlyJavaBeanObjectProperty(Object bean, String name) throws NoSuchMethodException {
        return ReadOnlyJavaBeanObjectPropertyBuilder.<T>create().bean(bean).name(name).build();
    }

}
