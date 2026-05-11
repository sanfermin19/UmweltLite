package com.fermine.umweltlite.api.entity;

import com.fermine.umweltlite.engine.UmweltEngine;

/**
 * An interface to be applied to any Mob that supports the Umwelt architecture.
 * This allows the UmweltAPI to interact with the entity without needing
 * to check goal selectors.
 */
public interface IUmweltEntity {
    UmweltEngine getUmweltEngine();

    // Quick-access for physical layer checks
    default boolean isUmweltActive() {
        return getUmweltEngine() != null && !getUmweltEngine().isExhausted();
    }
}