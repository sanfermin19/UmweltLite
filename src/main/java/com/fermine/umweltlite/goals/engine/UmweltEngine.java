package com.fermine.umweltlite.goals.engine;

import com.fermine.umweltlite.goals.engine.body.engine.BodyEngine;
import com.fermine.umweltlite.goals.engine.emotion.engine.EmotionalEngine;
import com.fermine.umweltlite.goals.engine.knowledge.engine.KnowledgeEngine;
import com.fermine.umweltlite.goals.engine.memory.engine.MemoryEngine;
import com.fermine.umweltlite.goals.engine.personality.engine.PersonalityEngine;
import com.fermine.umweltlite.goals.engine.sensory.engine.SensoryEngine;
import com.fermine.umweltlite.processor.UmweltProcessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The central coordinator for the UmweltLite simulation. Orchestrates the
 * data pipeline lifecycle across sensory, emotional, memory, and physical motor systems.
 */
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

    /**
     * Processors with a priority number above this threshold are skipped when the entity is exhausted.
     * Matches the default cutoff in UmweltProcessor so library users can reason about it without reading engine source.
     */
    public static final int EXHAUSTION_PRIORITY_CUTOFF = UmweltProcessor.EXHAUSTION_CUTOFF;

    public UmweltEngine(Mob mob) {
        this.mob = mob;
        this.personalityEngine = new PersonalityEngine(mob.getUUID().getLeastSignificantBits());

        // Initialize the body systems so the physical layer is active immediately
        this.bodyEngine.setupBody(null);
    }

    /**
     * Executes the standard processing cycle: Consolidate -> Read -> Think -> Act.
     */
    public StorageInsert tick(Mob mob) {
        if (mob == null) return this.lastIntent;

        // 1. CONSOLIDATE: Update all sensory and internal state architectures
        this.sensoryEngine.tick(mob, this);
        this.emotionalEngine.tick(mob);
        this.knowledgeEngine.tick(mob.level().getGameTime());
        this.memoryEngine.tick(mob);

        // Promote high-retention short-term memories into long-term spatial knowledge every 5 seconds.
        // Batched rather than every tick to keep the consolidation cost negligible.
        if (mob.tickCount % 100 == 0) {
            this.memoryEngine.consolidateToKnowledge(this);
        }

        // 2. READ: Synthesize a clean data snapshot for processors to digest
        StorageRetrieval snapshot = this.getSnapshot();

        // 3. THINK: Run active processors using optimized indexed looping to prevent Iterator allocations
        StorageInsert currentMasterIntent = StorageInsert.idle();

        for (UmweltProcessor processor : this.processors) {
            // Priority ceiling gate: bypass low-priority thoughts if system is exhausted
            if (this.isExhausted && processor.priority() > EXHAUSTION_PRIORITY_CUTOFF
                    && !processor.isExhaustionExempt()) {
                continue;
            }

            StorageInsert result = processor.tick(this, mob, snapshot);
            if (result != null) {
                currentMasterIntent = this.mergeIntents(currentMasterIntent, result);
            }
        }

        // 4. ACT: Commit finalized intent state to the BodyEngine framework for navigation execution
        this.lastIntent = currentMasterIntent;

        // Handles yaw resolution, step safety calculations, and physics translation
        this.bodyEngine.tick(mob, this, snapshot, currentMasterIntent);

        return currentMasterIntent;
    }

    /**
     * Blends processing layers together based on directional length thresholds and explicit animation states.
     */
    private StorageInsert mergeIntents(StorageInsert base, StorageInsert override) {
        // Fallback checks to guarantee safe processing
        if (base == null) return override != null ? override : StorageInsert.idle();
        if (override == null) return base;

        // Allocate vector priorities based on squared epsilon length boundaries
        net.minecraft.world.phys.Vec3 drive = override.driveVector().lengthSqr() > 1.0E-4
                ? override.driveVector()
                : base.driveVector();

        int jump = (int) Math.max(base.jumpUrge(), override.jumpUrge());

        // Optimized Optional resolution pattern to bypass functional supplier allocations inside the ticking cycle
        java.util.Optional<String> anim = override.animation().isPresent()
                ? override.animation()
                : base.animation();

        return new StorageInsert(drive, jump, anim);
    }

    /**
     * Pulls current immutable values out of sub-engines to form a frame evaluation packet.
     */
    public StorageRetrieval getSnapshot() {
        return new StorageRetrieval(
                this.sensoryEngine.getIntake(),
                this.emotionalEngine.getSocialSnapshot(),
                this.emotionalEngine.getCurrentMap()
        );
    }

    // API & Internal Getters
    public Mob getMob() { return this.mob; }
    public StorageInsert getLastIntent() { return this.lastIntent; }
    public KnowledgeEngine getKnowledgeEngine() { return this.knowledgeEngine; }
    public EmotionalEngine getEmotionalEngine() { return this.emotionalEngine; }
    public SensoryEngine getSensoryEngine() { return this.sensoryEngine; }
    public MemoryEngine getMemoryEngine() { return this.memoryEngine; }
    public PersonalityEngine getPersonality() { return this.personalityEngine; }
    public BodyEngine getBodyEngine() { return this.bodyEngine; }

    public boolean isExhausted() { return this.isExhausted; }
    public void setExhausted(boolean state) { this.isExhausted = state; }

    /**
     * Registers a new processing element and updates execution sequences.
     */
    public void addProcessor(UmweltProcessor processor) {
        if (processor == null) return;
        this.processors.add(processor);
        this.processors.sort(Comparator.comparingInt(UmweltProcessor::priority));
    }

    // --- NeoForge 1.21.1 Capability/Data Serialization Implementation ---

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

    /**
     * Swaps a standard entity instance with its custom Umwelt-driven equivalent.
     */
    public static void swapWithUmwelt(Mob vanilla, EntityType<? extends Mob> umweltType) {
        if (vanilla == null || vanilla.level().isClientSide || umweltType == null) return;

        Mob umwelt = umweltType.create(vanilla.level());
        if (umwelt != null) {
            // Transfer spatial variables, status values, and custom metadata strings
            umwelt.moveTo(vanilla.getX(), vanilla.getY(), vanilla.getZ(), vanilla.getYRot(), vanilla.getXRot());
            umwelt.setHealth(vanilla.getHealth());

            if (vanilla.hasCustomName()) {
                umwelt.setCustomName(vanilla.getCustomName());
            }

            vanilla.level().addFreshEntity(umwelt);
            vanilla.discard(); // Mark old standard entity instance for deletion
        }
    }

    public List<UmweltProcessor> getProcessors() {
        return this.processors;
    }
}