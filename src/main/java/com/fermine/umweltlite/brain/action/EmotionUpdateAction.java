package com.fermine.umweltlite.brain.action;

import net.minecraft.world.entity.LivingEntity;

public class EmotionUpdateAction implements IBrainAction {
    private final String emotionType;
    private final float intensity;

    public EmotionUpdateAction(String emotionType, float intensity) {
        this.emotionType = emotionType;
        this.intensity = intensity;
    }

    @Override
    public String getActionId() {
        return "emotion_update";
    }

    @Override
    public void execute(LivingEntity entity) {
        // Server-side: Update current emotional states/appraisal tracking variables
        // if necessary before syncing down to clients.
    }

    public String getEmotionType() { return emotionType; }
    public float getIntensity() { return intensity; }
}