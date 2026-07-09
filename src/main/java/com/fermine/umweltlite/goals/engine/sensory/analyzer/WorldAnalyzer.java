package com.fermine.umweltlite.goals.engine.sensory.analyzer;

import com.fermine.umweltlite.goals.engine.sensory.perception.WorldPerception;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class WorldAnalyzer {

    public static WorldPerception analyze(Mob mob) {
        if (mob == null) {
            return WorldPerception.EMPTY;
        }
        Level level = mob.level();

        // 1. Light and Exposure (Objective physical metrics)
        float rawLight = level.getMaxLocalRawBrightness(mob.blockPosition());
        float light = Mth.clamp(rawLight / 15.0f, 0.0f, 1.0f);

        float exposure = level.canSeeSky(mob.blockPosition()) ? 1.0f : 0.0f;

        // 2. Raw Time Signal (Defaulting to the sine wave tracking)
        long dayTime = level.getDayTime() % 24000;
        float timeSignal = Mth.sin((float) ((dayTime - 6000) * Math.PI / 12000.0f)) * 0.5f + 0.5f;

        // 3. Weather Intensity (Objective rain/thunder values)
        float rain = UmweltNBTUtils.safeFloat(level.getRainLevel(1.0f), 0.0f);
        float thunder = UmweltNBTUtils.safeFloat(level.getThunderLevel(1.0f), 0.0f);
        float weather = Mth.clamp(Math.max(rain, thunder), 0.0f, 1.0f);

        return new WorldPerception(
                light,
                exposure,
                timeSignal,
                weather
        );
    }

    private WorldAnalyzer() {}
}