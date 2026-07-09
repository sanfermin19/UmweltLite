package com.fermine.umweltlite.brain.inter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/**
 * Standardized behavioral primitive for the Cognitive Triad Architecture.
 * Actions are evaluated dynamically by the voting matrix based on a snapshot of environmental and metabolic registers.
 */
public interface IUmweltAction {

    /**
     * @return Unique identifier for this action instance, crucial for client-side diagnostic synchronization.
     */
    ResourceLocation getActionId();

    /**
     * @return The psychological category or domain group this behavior answers to (e.g., "survival", "self_interest", "analytical").
     */
    String getCategory();

    /**
     * Evaluates the current psychological registers of the organism to vote on this action's priority.
     * Higher returns represent extreme cognitive urgency.
     *
     * @param host            The mob entity processing its brain pipeline.
     * @param analytical      The logical/social register weight (0.0F to 1.0F).
     * @param survival        The panic/adrenaline reactive register weight (0.0F to 1.0F).
     * @param selfInterest    The metabolic/desire register weight (0.0F to 1.0F).
     * @return A priority score, typically bounded between 0.0F and 1.0F (can exceed for hard overrides).
     */
    float calculateUrgency(Mob host, float analytical, float survival, float selfInterest);

    /**
     * Initial execution hook triggered exactly once when the voting matrix switches to this action.
     * Used to reset navigation paths, allocate memory registers, or trigger start animations.
     */
    default void start(Mob host) {}

    /**
     * Executed every tick while this action remains the dominant choice of the cognitive pipeline.
     */
    void tick(Mob host);

    /**
     * Clean-up hook triggered exactly once when the pipeline interrupts or switches away from this behavior.
     * Used to clean up targets, clear navigation flags, or stop specific visual animations.
     */
    default void stop(Mob host) {}
}