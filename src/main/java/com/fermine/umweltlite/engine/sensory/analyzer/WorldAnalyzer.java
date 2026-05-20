package com.fermine.umweltlite.engine.sensory.analyzer;

import com.fermine.umweltlite.engine.sensory.perception.WorldPerception;
import com.fermine.umweltlite.utils.UmweltNBTUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public final class WorldAnalyzer {

    public static WorldPerception analyze(Mob mob) {
        Level level = mob.level();

        // 1. Light and Exposure
        // Clamping light ensures that if a mod sets brightness > 15, we don't break the 0.0-1.0 scale
        float rawLight = level.getMaxLocalRawBrightness(mob.blockPosition());
        float light = Mth.clamp(rawLight / 15.0f, 0.0f, 1.0f);

        float exposure = level.canSeeSky(mob.blockPosition()) ? 1.0f : 0.0f;

        // 2. Time Signal (The Sine Wave)
        // This is safe, but we'll use Mth.sin for consistent math performance
        long dayTime = level.getDayTime() % 24000;
        float timeSignal = Mth.sin((float) ((dayTime - 6000) * Math.PI / 12000.0f)) * 0.5f + 0.5f;

        // 3. Weather Intensity
        // Getting rain/thunder levels can sometimes return NaN in bugged custom dimensions
        float rain = UmweltNBTUtils.safeFloat(level.getRainLevel(1.0f), 0.0f);
        float thunder = UmweltNBTUtils.safeFloat(level.getThunderLevel(1.0f), 0.0f);
        float weather = Mth.clamp(Math.max(rain, thunder), 0.0f, 1.0f);

        // 4. Final Perception Wrap
        return new WorldPerception(
                light,
                exposure,
                UmweltNBTUtils.safeFloat(timeSignal, 0.5f),
                weather
        );
    }

    private WorldAnalyzer() {}
}