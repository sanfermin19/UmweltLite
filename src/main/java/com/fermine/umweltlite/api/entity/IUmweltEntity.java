package com.fermine.umweltlite.api.entity;

import com.fermine.umweltlite.goals.engine.UmweltEngine;

public interface IUmweltEntity {
    UmweltEngine getUmweltEngine();

    // Fixed the logic here. It should be active if it is NOT exhausted (or whatever your intent was).
    default boolean isUmweltActive() {
        return getUmweltEngine() != null && !getUmweltEngine().isExhausted();
    }

    boolean isResting();
    boolean isProcessing();
    void setProcessing(boolean active);
}