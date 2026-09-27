package com.cookielauncher.core.fakefx.event;

import java.util.Set;

public interface CompositeEventTarget extends EventTarget {
    Set<EventTarget> getTargets();

    boolean containsTarget(EventTarget target);
}
