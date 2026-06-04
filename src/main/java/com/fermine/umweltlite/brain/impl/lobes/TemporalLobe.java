package com.fermine.umweltlite.brain.impl.lobes;

import com.fermine.umweltlite.brain.inter.lobes.ITemporalLobe;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;

public class TemporalLobe implements ITemporalLobe {

    @Override
    public void tick(LivingEntity host) {
        // Passive auditory processing or processing semantic cues could live here
    }

    @Override
    public boolean isRecognizedThreat(LivingEntity entity) {
        if (entity instanceof Player player) {
            // If they are holding weapons or sprint-charging, categorize as a clear threat
            return player.getMainHandItem().isDamageableItem() || player.isSprinting();
        }
        // Universal hostile entity categorization fallback
        return !entity.getType().getCategory().isFriendly();
    }

    @Override
    public String getEntitySocialClassification(LivingEntity entity) {
        if (entity instanceof Cow) {
            return "HERD_MEMBER";
        } else if (entity instanceof Player) {
            return "APEX_PREDATOR";
        }
        return "UNKNOWN_ORGANISM";
    }

    @Override
    public void reset() {}
}