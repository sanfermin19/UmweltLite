package com.fermine.umweltlite.brain.impl.lobes;

import com.fermine.umweltlite.brain.impl.sections.Amygdala;
import com.fermine.umweltlite.brain.impl.sections.BrainStem;
import com.fermine.umweltlite.brain.impl.sections.Hippocampus;
import com.fermine.umweltlite.brain.inter.IBrainComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class FrontalLobe implements IBrainComponent {

    // Internal Sub-Modules & Cross-Talk Connections
    private final PrefrontalCortex pfc;
    private final BrainStem brainStem;
    private final Amygdala amygdala;
    private final Hippocampus hippocampus;
    private final OccipitalLobe occipitalLobe;
    private final ParietalLobe parietalLobe;

    // Executive States
    private float decisionFatigue = 0.0f; // 0.0 (Sharp) to 1.0 (Completely Paralyzed/Overwhelmed)
    private Vec3 activeMotorVector = Vec3.ZERO;

    public FrontalLobe(BrainStem stem, Amygdala amygdala, Hippocampus hippocampus,
                       OccipitalLobe occipital, ParietalLobe parietal) {
        this.brainStem = stem;
        this.amygdala = amygdala;
        this.hippocampus = hippocampus;
        this.occipitalLobe = occipital;
        this.parietalLobe = parietal;
        this.pfc = new PrefrontalCortex();
    }

    @Override
    public void tick(LivingEntity host) {
        // 1. Recover decision energy slowly if intent remains stable
        this.decisionFatigue = Math.max(0.0f, this.decisionFatigue - 0.002f);

        // 2. Tick the Prefrontal Cortex to determine the active "Will"
        this.pfc.tick(host);

        // 3. Translate the active Will into a physical velocity pathing plan
        this.activeMotorVector = calculateMotorOutput(host, this.pfc.getCurrentWill());
    }

    private Vec3 calculateMotorOutput(LivingEntity host, String activeWill) {
        // If experiencing complete neurological collapse, lock up the motor pathways entirely
        if (this.pfc.isExperiencingHelplessness() || this.decisionFatigue > 0.9f) {
            return Vec3.ZERO;
        }

        switch (activeWill) {
            case "FLEED_PANIC":
                // Generate a fleeing vector directly away from a focused visual threat target
                Optional<UUID> targetUUID = occipitalLobe.getFocusedTargetUUID();
                if (targetUUID.isPresent()) {
                    net.minecraft.world.entity.Entity threat = host.level().getPlayerByUUID(targetUUID.get());
                    if (threat == null) threat = host.level().getEntity(host.getId()); // Fallback bounds

                    if (threat != null) {
                        return host.position().subtract(threat.position()).normalize().scale(1.25);
                    }
                }
                // Blind blind-fleeing fallback
                return host.getLookAngle().reverse().scale(1.0);

            case "SEEK_REFUGE":
                // Steer along path coordinates toward recorded memory safe zones
                Optional<BlockPos> safeZone = hippocampus.getStableSafeZone();
                if (safeZone.isPresent()) {
                    Vec3 targetSpace = Vec3.atCenterOf(safeZone.get());
                    return targetSpace.subtract(host.position()).normalize().scale(0.8);
                }
                return Vec3.ZERO;

            case "REST":
                // Slow biological deceleration to settle body position
                return host.getDeltaMovement().scale(0.5);

            case "STROLL_FORAGE":
            default:
                // Normal wander parameters: gentle sway pathing vector based on host facing orientation
                if (host.getRandom().nextFloat() < 0.05f) {
                    this.decisionFatigue += 0.02f; // Slight fatigue accumulation from trivial choices
                    float angle = host.getRandom().nextFloat() * 2.0f * (float) Math.PI;
                    return new Vec3(Math.cos(angle) * 0.3, 0, Math.sin(angle) * 0.3);
                }
                return host.getDeltaMovement();
        }
    }

    @Override
    public void reset() {
        this.decisionFatigue = 0.0f;
        this.activeMotorVector = Vec3.ZERO;
        this.pfc.reset();
    }

    // High Level Accessors
    public PrefrontalCortex getPrefrontalCortex() { return this.pfc; }
    public Vec3 getActiveMotorVector() { return this.activeMotorVector; }
    public float getDecisionFatigue() { return this.decisionFatigue; }

    // =========================================================================
    // NESTED EXECUTIVE SUB-MODULE: THE PREFRONTAL CORTEX
    // =========================================================================
    public class PrefrontalCortex implements IBrainComponent {

        private String currentWill = "STROLL_FORAGE";
        private boolean isHelpless = false;

        @Override
        public void tick(LivingEntity host) {
            float panic = amygdala.getPanicFloater();
            float pain = parietalLobe.getPainIndex();

            // 1. EVALUATE TRAUMA HISTORY FOR LEARNED HELPLESSNESS
            // Look into the focus memory registry. If we see a known threat and trauma is maximum...
            Optional<UUID> visibleThreat = occipitalLobe.getFocusedTargetUUID();
            if (visibleThreat.isPresent()) {
                float accumulatedTrauma = hippocampus.getTraumaLevel(visibleThreat.get());

                // Cognitive Boundary Condition: Max pain paired with absolute max trauma collapses intention
                if (accumulatedTrauma > 0.85f && pain > 0.6f) {
                    this.isHelpless = true;
                    this.currentWill = "COLLAPSE_SHUTDOWN";
                    return;
                }
            }

            // Recovery check: Shock fading down can shake the mind loose from a helpless freeze state
            if (panic < 0.2f && pain < 0.1f) {
                this.isHelpless = false;
            }

            if (this.isHelpless) {
                this.currentWill = "COLLAPSE_SHUTDOWN";
                return;
            }

            // 2. STANDARD EXECUTIVE DRIVE SELECTOR
            if (panic > 0.6f) {
                // High fear overrides base wander drives
                if (hippocampus.getStableSafeZone().isPresent() && panic < 0.8f) {
                    this.currentWill = "SEEK_REFUGE";
                } else {
                    this.currentWill = "FLEED_PANIC";
                }
                FrontalLobe.this.decisionFatigue += 0.05f; // Intense emergency states increase choices fatigue
            } else if (brainStem.getExhaustion() > 0.75f || pain > 0.4f) {
                // Metabolic or physical exhaustion demands down-regulation
                this.currentWill = "REST";
            } else {
                this.currentWill = "STROLL_FORAGE";
            }
        }

        @Override
        public void reset() {
            this.currentWill = "STROLL_FORAGE";
            this.isHelpless = false;
        }

        public String getCurrentWill() { return this.currentWill; }
        public boolean isExperiencingHelplessness() { return this.isHelpless; }
    }
}