package com.fermine.umweltlite.engine;

import com.fermine.umweltlite.engine.body.engine.BodyEngine;
import com.fermine.umweltlite.engine.emotion.engine.EmotionalEngine;
import com.fermine.umweltlite.engine.knowledge.engine.KnowledgeEngine;
import com.fermine.umweltlite.engine.memory.engine.MemoryEngine;
import com.fermine.umweltlite.engine.personality.engine.PersonalityEngine;
import com.fermine.umweltlite.engine.sensory.engine.SensoryEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UmweltEngine {
    private final Mob mob;

    // Core Sub-Engines
    private final SensoryEngine sensoryEngine = new SensoryEngine();
    private final EmotionalEngine emotionalEngine = new EmotionalEngine();
    private final KnowledgeEngine knowledgeEngine = new KnowledgeEngine();
    private final MemoryEngine memoryEngine = new MemoryEngine();
    private final PersonalityEngine personalityEngine;

    // Physical Layer
    private final BodyEngine bodyEngine = new BodyEngine();

    private final List<UmweltProcessor> processors = new ArrayList<>();
    private StorageInsert lastIntent = StorageInsert.idle();
    private boolean isExhausted = false;

    public UmweltEngine(Mob mob) {
        this.mob = mob;
        this.personalityEngine = new PersonalityEngine(mob.getUUID().getLeastSignificantBits());

        // Initialize the body systems so the physical layer is active immediately
        this.bodyEngine.setupBody(null);
    }

    public StorageInsert tick(Mob mob) {
        // 1. CONSOLIDATE: Update all sensory and internal state data
        sensoryEngine.tick(mob, this);
        emotionalEngine.tick(mob);
        knowledgeEngine.tick(mob.level().getGameTime());
        memoryEngine.tick(mob);

        // 2. READ: Create the data snapshot for processors to digest
        StorageRetrieval snapshot = this.getSnapshot();

        // 3. THINK: Run active processors to determine intent
        StorageInsert currentMasterIntent = StorageInsert.idle();
        for (UmweltProcessor processor : this.processors) {
            if (this.isExhausted && processor.priority() > 20) continue;

            StorageInsert result = processor.tick(this, mob, snapshot);
            currentMasterIntent = mergeIntents(currentMasterIntent, result);
        }

        // 4. ACT: Pass the finalized intent to the BodyEngine for physical execution
        this.lastIntent = currentMasterIntent;

        // This bridge handles rotation, navigation safety, and movement validation
        bodyEngine.tick(mob, this, snapshot, currentMasterIntent);

        return currentMasterIntent;
    }

    private StorageInsert mergeIntents(StorageInsert base, StorageInsert override) {
        return new StorageInsert(
                // Use override vector if it has length, otherwise stick with base
                override.driveVector().lengthSqr() > 1.0E-4 ? override.driveVector() : base.driveVector(),
                Math.max(base.jumpUrge(), override.jumpUrge()),
                override.animation().or(base::animation)
        );
    }

    public StorageRetrieval getSnapshot() {
        return new StorageRetrieval(
                sensoryEngine.getIntake(),
                emotionalEngine.getSocialSnapshot(),
                emotionalEngine.getCurrentMap()
        );
    }

    // API & Internal Getters
    public Mob getMob() { return this.mob; }
    public StorageInsert getLastIntent() { return this.lastIntent; }
    public KnowledgeEngine getKnowledgeEngine() { return knowledgeEngine; }
    public EmotionalEngine getEmotionalEngine() { return emotionalEngine; }
    public SensoryEngine getSensoryEngine() { return sensoryEngine; }
    public MemoryEngine getMemoryEngine() { return memoryEngine; }
    public PersonalityEngine getPersonality() { return personalityEngine; }
    public BodyEngine getBodyEngine() { return bodyEngine; }

    public boolean isExhausted() { return isExhausted; }
    public void setExhausted(boolean state) { this.isExhausted = state; }

    public void addProcessor(UmweltProcessor processor) {
        this.processors.add(processor);
        this.processors.sort(Comparator.comparingInt(UmweltProcessor::priority));
    }

    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("knowledge", this.knowledgeEngine.serializeNBT(provider));
        tag.put("emotional", this.emotionalEngine.serializeNBT(provider));
        tag.put("memory", this.memoryEngine.serializeNBT(provider));
        tag.putBoolean("exhausted", this.isExhausted);
        return tag;
    }

    public void deserializeNBT(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag tag) {
        if (tag.contains("knowledge")) {
            this.knowledgeEngine.deserializeNBT(provider, tag.getCompound("knowledge"));
        }
        if (tag.contains("emotional")) {
            this.emotionalEngine.deserializeNBT(provider, tag.getCompound("emotional"));
        }
        if (tag.contains("memory")) {
            this.memoryEngine.deserializeNBT(provider, tag.getCompound("memory"));
        }
        this.isExhausted = tag.getBoolean("exhausted");
    }

    public static void swapWithUmwelt(Mob vanilla, EntityType<? extends Mob> umweltType) {
        if (vanilla.level().isClientSide) return;

        Mob umwelt = umweltType.create(vanilla.level());
        if (umwelt != null) {
            // Transfer the "Legacy": Position, Health, and even Name Tags
            umwelt.moveTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(), vanilla.getYRot(), vanilla.getXRot());
            umwelt.setHealth(vanilla.getHealth());
            if (vanilla.hasCustomName()) umwelt.setCustomName(vanilla.getCustomName());

            vanilla.level().addFreshEntity(umwelt);
            vanilla.discard(); // The old mob is "upgraded"
        }
    }

    public List<UmweltProcessor> getProcessors() {
        return this.processors;
    }
}