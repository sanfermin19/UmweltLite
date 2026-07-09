package com.fermine.umweltlite.brain.handler;

import com.fermine.umweltlite.UmweltLite;
import com.fermine.umweltlite.brain.cognitive.ICognitiveOrganism;
import com.fermine.umweltlite.brain.capability.UmweltAttachments;
import com.fermine.umweltlite.brain.inter.IUmweltBrainPipeline;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Global tick handler responsible for driving registered cognitive pipelines
 * on the server-authoritative logic thread.
 */
@EventBusSubscriber(modid = UmweltLite.MODID)
public class UmweltBrainTicker {

    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Post event) {
        // Core sanity check: Brain logic must run strictly on the server thread context
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (event.getEntity() instanceof Mob mob) {
            /*
             * Modernized double-tick protection framework.
             * If the entity implements our clean API surface AND wants to manually orchestrate
             * its own cognitive updates inside its overridden tick loops, we step out of the way.
             */
            if (mob instanceof ICognitiveOrganism cognitiveMob && cognitiveMob.isCognitiveEngineActive()) {
                // If the developer wants custom manual handling, skip global auto-ticking
                return;
            }

            try {
                // Read from our unified attachment register instead of the old broken classes
                if (mob.hasData(UmweltAttachments.BRAIN_PIPELINE)) {
                    IUmweltBrainPipeline pipeline = mob.getData(UmweltAttachments.BRAIN_PIPELINE);
                    pipeline.tickBrainPipeline(mob);
                }
            } catch (IllegalStateException e) {
                // Catch and swallow unexpected registry phase lookups during asynchronous world generation ticks
                UmweltLite.LOGGER.warn("Encountered thread safety race condition during early world-gen brain attachment lookup for entity: {}", mob.getType());
            }
        }
    }
}