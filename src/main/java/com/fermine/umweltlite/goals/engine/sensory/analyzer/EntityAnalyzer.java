package com.fermine.umweltlite.goals.engine.sensory.analyzer;

import com.fermine.umweltlite.goals.engine.sensory.perception.EntityPerception;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class EntityAnalyzer {

    public static EntityPerception analyze(LivingEntity target, Mob observer) {
        // 1. EXISTENCE GUARD
        if (target == null || !target.isAlive() || target == observer) {
            return EntityPerception.EMPTY;
        }

        // 2. PHYSICAL PRESENCE (Form Factor)
        float width = target.getBbWidth();
        float height = target.getBbHeight();
        float volume = width * width * height;
        float formFactor = Mth.clamp(volume / 4.0f, 0.0f, 1.0f);

        // 3. AGGRESSION & DIRECTIONAL THREAT
        float aggression = 0.0f;
        if (target instanceof Enemy) aggression += 0.4f;
        if (target instanceof Player p && !p.isCreative()) aggression += 0.2f;

        // --- THE NaN FIREWALL ---
        Vec3 diff = observer.position().subtract(target.position());
        double distanceSq = diff.lengthSqr();

        if (distanceSq > 0.001) {
            Vec3 lookVec = target.getViewVector(1.0f);
            Vec3 relativeDir = diff.normalize();
            double dot = lookVec.dot(relativeDir);

            // Objective vector match tracking: is the entity pointing at the observer?
            if (dot > 0.8) {
                aggression += 0.3f;
            }
        }

        // 4. VITALITY & BASELINE THREAT
        float vitality = Mth.clamp(target.getHealth() / Math.max(target.getMaxHealth(), 1.0f), 0.0f, 1.0f);
        float threat = Mth.clamp(aggression + (formFactor * 0.5f), 0.0f, 1.0f);

        // 5. SOCIAL WEIGHT
        float social = 0.0f;
        if (target.getType() == observer.getType()) {
            social = 0.8f;
        } else if (!(target instanceof Enemy)) {
            social = 0.2f;
        }

        // 6. APEX STATUS
        boolean isApex = (formFactor > 0.7f && aggression > 0.3f) || (target instanceof Enemy && volume > 2.0f);

        return new EntityPerception(
                threat,
                social,
                vitality,
                formFactor,
                isApex
        );
    }

    private EntityAnalyzer() {}
}