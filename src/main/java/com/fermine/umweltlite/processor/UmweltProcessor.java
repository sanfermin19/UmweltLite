package com.fermine.umweltlite.processor;


import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;

public interface UmweltProcessor {
    StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot);
    default int priority() { return 50; }
}