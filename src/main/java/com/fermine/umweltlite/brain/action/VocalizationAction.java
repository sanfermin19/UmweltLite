package com.fermine.umweltlite.brain.action;


import net.minecraft.world.entity.LivingEntity;

public class VocalizationAction implements IBrainAction {
    private final String messageKey;

    public VocalizationAction(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String getActionId() {
        return "vocalization";
    }

    @Override
    public void execute(LivingEntity entity) {
        // Server-side logging or tracking of social output
    }

    public String getMessageKey() { return messageKey; }
}