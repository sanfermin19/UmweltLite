package com.fermine.umweltlite.impl.engine.personality.engine;

import com.fermine.umweltlite.impl.engine.sensory.engine.SensoryIntake;
import com.fermine.umweltlite.impl.engine.sensory.raycast.RaycastResult;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages an entity's psychological makeup, tracking overrides, template mutations,
 * and transforming real-time objective sensory inputs into subjective behavioral weights.
 */
public class PersonalityEngine {
    private final long personalitySeed;
    private final Map<String, Float> activeTraits = new ConcurrentHashMap<>();
    private final Map<String, Float> overriddenTraits = new ConcurrentHashMap<>();

    public PersonalityEngine(long seed) {
        this.personalitySeed = seed;
        this.generateFeralTraits();
    }

    private void generateFeralTraits() {
        RandomSource random = RandomSource.create(this.personalitySeed);
        activeTraits.put("bravery", random.nextFloat());
        activeTraits.put("empathy", random.nextFloat());
        activeTraits.put("anxiety", random.nextFloat());
        activeTraits.put("playfulness", random.nextFloat());
        activeTraits.put("analytical", random.nextFloat());
        activeTraits.put("focus", random.nextFloat());
    }

    public void applyTemplate(PersonalityTemplate template) {
        if (template == null) return;
        RandomSource random = RandomSource.create(this.personalitySeed);

        template.baseTraits().forEach((trait, baseValue) -> {
            float offset = (random.nextFloat() - 0.5f) * template.variance();
            activeTraits.put(trait, Mth.clamp(baseValue + offset, 0.0f, 1.0f));
        });
    }

    public void setOverride(String trait, Float value) {
        if (value == null) {
            overriddenTraits.remove(trait);
        } else {
            overriddenTraits.put(trait, Mth.clamp(value, 0.0f, 1.0f));
        }
    }

    public float getTrait(String key) {
        Float override = overriddenTraits.get(key);
        if (override != null) {
            return override;
        }
        return activeTraits.getOrDefault(key, 0.5f);
    }

    /**
     * Translates pure mathematical observations into filtered subjective matrices.
     * Uses focus and analytical traits to scale awareness levels with zero allocations.
     */
    public AppraisalResult appraise(SensoryIntake intake) {
        if (intake == null) return AppraisalResult.CALM;

        // Cache primitives to optimize calculation operations
        final float bravery = getTrait("bravery");
        final float anxiety = getTrait("anxiety");
        final float empathy = getTrait("empathy");
        final float playfulness = getTrait("playfulness");
        final float analytical = getTrait("analytical");
        final float focus = getTrait("focus");

        final float neuroticism = Mth.clamp((anxiety - bravery + 1.0f) * 0.5f, 0.0f, 1.0f);

        float totalThreat = 0.0f;
        float totalCuriosity = 0.0f;
        float totalSocial = 0.0f;

        // ==========================================
        // 1. ENVIRONMENTAL & ATMOSPHERIC FILTERING
        // ==========================================
        float objectiveLight = intake.worldState.lightLevel();
        float objectiveWeather = intake.worldState.weatherIntensity();
        float subjectiveTimeSignal = intake.worldState.timeSignal();

        // Low focus entities register atmospheric elements poorly unless severe
        float subjectiveWeather = objectiveWeather;
        if (focus < 0.4f) {
            subjectiveWeather = objectiveWeather > 0.6f ? objectiveWeather : 0.0f;
        }

        // High analytical processing scales sensitivity to structural environmental cycles
        if (analytical > 0.7f && intake.mobRef() != null) {
            // Recalculate linear day-time tracking loop ratio for exact behavioral branches
            long rawDayTime = intake.mobRef().level().getDayTime() % 24000;
            subjectiveTimeSignal = Mth.clamp((float) rawDayTime / 24000.0f, 0.0f, 1.0f);
        }

        // Apply dark atmospheric modifiers using our filtered light values
        if (objectiveLight < 0.20f) {
            float darknessFear = anxiety * 0.35f;
            if (darknessFear > totalThreat) {
                totalThreat = darknessFear;
            }
        }

        // Process weather thresholds using our modified subjective weather intensity
        if (subjectiveWeather > 0.4f) { // Equates to our previous 'isStorming()' check logic
            totalThreat = Math.max(totalThreat, anxiety * 0.25f);
            totalCuriosity *= (1.0f - (anxiety * 0.5f));
        }

        // ==========================================
        // 2. SPATIAL RAYCAST PROCESSING
        // ==========================================
        RaycastResult centerRay = intake.getRay("center");
        if (centerRay != null && centerRay.isBlocked()) {
            if (centerRay.isEntity()) {
                totalThreat += 0.25f * neuroticism;
                totalCuriosity += 0.20f * playfulness * focus;
                totalSocial += 0.15f * empathy;
            } else {
                totalCuriosity += 0.10f * playfulness * focus;
            }
        }

        // ==========================================
        // 3. SUBJECTIVE APPRAISAL OF WITNESSED ENTITIES
        // ==========================================
        int observedEntityCount = intake.entities.size();
        for (int i = 0; i < observedEntityCount; i++) {
            SensoryIntake.EntityObservation obs = intake.entities.get(i);
            if (obs == null || obs.entity() == null || obs.perception() == null) continue;

            float baseThreat = obs.perception().threatLevel();
            float baseSocial = obs.perception().socialValue();

            // Analytical minds accurately extrapolate danger, while high focus locks on
            float threatLens = (0.5f + neuroticism) * (0.8f + (analytical * 0.4f));
            float modifiedThreat = baseThreat * threatLens * obs.intensity();

            if (focus > 0.7f && modifiedThreat > 0.3f) {
                modifiedThreat = Math.min(modifiedThreat * 1.25f, 1.0f); // Hyper-focused panic spike
            }

            float modifiedSocial = baseSocial * (0.4f + (empathy * 1.2f)) * obs.intensity();
            float modifiedCuriosity = (1.0f - baseThreat) * (playfulness * 0.6f) * focus * obs.intensity();

            // Senses interaction (audio context modifier inside dark parameters)
            if ("audio".equals(obs.senseType()) && objectiveLight < 0.25f) {
                modifiedThreat *= (1.0f + anxiety);
            }

            if (modifiedThreat > totalThreat) totalThreat = modifiedThreat;
            if (modifiedSocial > totalSocial) totalSocial = modifiedSocial;
            if (modifiedCuriosity > totalCuriosity) totalCuriosity = modifiedCuriosity;
        }

        // ==========================================
        // 4. SUBJECTIVE APPRAISAL OF LOCAL BLOCKS
        // ==========================================
        int observedBlockCount = intake.blocks.size();
        for (int j = 0; j < observedBlockCount; j++) {
            SensoryIntake.BlockObservation blockObs = intake.blocks.get(j);
            if (blockObs == null || blockObs.perception() == null) continue;

            float baseDanger = blockObs.perception().dangerRating();
            float baseInterest = blockObs.perception().interestRating();

            // Un-analytical entities might ignore complex block structures or noise danger fields
            float cognitiveClarity = Mth.lerp(analytical, 0.4f, 1.1f);
            float blockThreat = baseDanger * (0.7f + (anxiety * 0.6f)) * cognitiveClarity;
            float blockCuriosity = baseInterest * (playfulness * 0.8f) * focus;

            if (blockThreat > totalThreat) totalThreat = blockThreat;
            if (blockCuriosity > totalCuriosity) totalCuriosity = blockCuriosity;
        }

        return new AppraisalResult(
                Mth.clamp(totalThreat, 0.0f, 1.0f),
                Mth.clamp(totalCuriosity, 0.0f, 1.0f),
                Mth.clamp(totalSocial, 0.0f, 1.0f)
        );
    }

    public Map<String, Float> getOverrides() {
        return Map.copyOf(this.overriddenTraits);
    }

    public Map<String, Float> getActiveTraits() {
        Map<String, Float> merged = new ConcurrentHashMap<>(this.activeTraits);
        merged.putAll(this.overriddenTraits);
        return merged;
    }
}