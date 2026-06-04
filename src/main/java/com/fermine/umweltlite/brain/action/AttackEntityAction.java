package com.fermine.umweltlite.brain.action;

import net.minecraft.world.entity.LivingEntity;

public class AttackEntityAction implements IBrainAction {
    // Renamed the variable to reflect it's the entity instance, not just an ID
    private final LivingEntity targetEntity;

    public AttackEntityAction(LivingEntity targetEntity) {
        this.targetEntity = targetEntity;
    }

    @Override
    public String getActionId() {
        return "attack_entity";
    }

    @Override
    public void execute(LivingEntity entity) {
        // Fix: Skip level lookup entirely since we already hold the target instance safely!
        if (this.targetEntity != null && this.targetEntity.isAlive()) {
            entity.doHurtTarget(this.targetEntity);
        }
    }

    public LivingEntity getTargetEntity() {
        return this.targetEntity;
    }
}