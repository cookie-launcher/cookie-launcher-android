package com.cookielauncher.core.fakefx.property.adapter;

import java.util.EventListener;

public interface VetoableChangeListener extends EventListener {
    void vetoableChange(PropertyChangeEvent var1) throws PropertyVetoException;
}