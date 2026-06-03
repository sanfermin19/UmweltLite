package com.fermine.umweltlite.impl.engine.body.engine;

import com.fermine.umweltlite.impl.engine.StorageInsert;
import com.fermine.umweltlite.impl.engine.StorageRetrieval;
import com.fermine.umweltlite.impl.engine.UmweltEngine;
import com.fermine.umweltlite.impl.engine.body.inter.IBodySystem;
import com.fermine.umweltlite.impl.engine.body.systems.MotorSystem;
import com.fermine.umweltlite.impl.engine.body.systems.SurvivalSystem;
import com.fermine.umweltlite.impl.engine.body.systems.VisionSystem;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BodyEngine {
    private final List<IBodySystem> systems = new ArrayList<>();
    private boolean initialized = false;
    private Object animationProvider;

    // Throttles path recalculations to save server TPS
    private int pathingCooldown = 0;
    private Vec3 lastTargetPos = Vec3.ZERO;

    public void setupBody(Object rawModel) {
        this.animationProvider = rawModel;

        this.systems.add(new MotorSystem());
        this.systems.add(new SurvivalSystem());
        this.systems.add(new VisionSystem());

        this.systems.sort(Comparator.comparing(IBodySystem::getSystemPriority));
        this.initialized = true;
    }

    public void tick(Mob mob, UmweltEngine engine, StorageRetrieval snapshot, StorageInsert intent) {
        if (!this.initialized) return;

        if (this.pathingCooldown > 0) {
            this.pathingCooldown--;
        }

        // 1. THE PULSE PHASE: Biological side effects & head tracking
        for (IBodySystem system : this.systems) {
            system.pulse(mob, engine, snapshot);
        }

        // 2. THE VECTOR PHASE: Execute the Intent
        Vec3 totalDrive = intent.driveVector();

        // 3. PHYSICAL CONSTRAINTS: Systems modify the drive
        for (IBodySystem system : this.systems) {
            totalDrive = totalDrive.add(system.getDriveVector(mob, engine, snapshot));
        }

        // 4. FINAL EXECUTION & SAFETY
        this.handleMovement(mob, totalDrive, intent);

        // Handle Animations placeholder
        intent.animation().ifPresent(animName -> {
            // Hook into your custom Proprio/Figura/GeckoLib engines safely here
        });
    }

    private void handleMovement(Mob mob, Vec3 drive, StorageInsert intent) {
        if (intent.jumpUrge() > 0.5f && mob.onGround()) {
            mob.jumpFromGround();
        }

        double sqrLen = drive.lengthSqr();
        if (sqrLen > 0.001 && Double.isFinite(drive.x + drive.z)) {

            // 1. Smooth Rotation Logic
            float targetYaw = (float) (Mth.atan2(drive.z, drive.x) * (180.0 / Math.PI)) - 90.0F;
            mob.setYRot(Mth.approachDegrees(mob.getYRot(), targetYaw, 15.0F));
            mob.yBodyRot = mob.getYRot();
            mob.yHeadRot = Mth.approachDegrees(mob.yHeadRot, targetYaw, 20.0F);

            // 2. Navigation Execution with Path Thrashing Protection
            if (this.pathingCooldown == 0) {
                // Securely normalize to ensure zero magnitudes never multiply into NaN fields
                Vec3 direction = sqrLen > 1.0 ? drive.normalize() : drive.scale(1.0 / Math.sqrt(sqrLen));
                Vec3 targetPos = mob.position().add(direction.scale(1.5));

                // Only request vanilla navigation if target shifted notably to maximize caching efficiency
                if (this.lastTargetPos == Vec3.ZERO || this.lastTargetPos.distanceToSqr(targetPos) > 0.25) {
                    mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0D);
                    this.lastTargetPos = targetPos;
                    this.pathingCooldown = 4; // Throttle navigation updates to 5 times per second max
                }
            }

        } else if (mob.getNavigation().isInProgress()) {
            mob.getNavigation().stop();
            this.lastTargetPos = Vec3.ZERO;
        }
    }

    public void addSystem(IBodySystem system) {
        this.systems.add(system);
        this.systems.sort(Comparator.comparing(IBodySystem::getSystemPriority));
    }

    public void removeSystem(Class<? extends IBodySystem> systemClass) {
        this.systems.removeIf(s -> s.getClass().equals(systemClass));
    }
}