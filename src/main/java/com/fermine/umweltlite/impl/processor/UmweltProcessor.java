package com.fermine.umweltlite.processor;


import com.fermine.umweltlite.impl.engine.StorageInsert;
import com.fermine.umweltlite.impl.engine.StorageRetrieval;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import net.minecraft.world.entity.Mob;

public interface UmweltProcessor {
    StorageInsert tick(UmweltEngine engine, Mob mob, StorageRetrieval snapshot);
    default int priority() { return 50; }
}