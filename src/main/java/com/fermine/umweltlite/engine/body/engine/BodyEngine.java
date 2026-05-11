package com.fermine.umweltlite.engine.body.engine;

import com.fermine.umweltlite.engine.StorageInsert;
import com.fermine.umweltlite.engine.StorageRetrieval;
import com.fermine.umweltlite.engine.UmweltEngine;
import com.fermine.umweltlite.engine.body.inter.IBodySystem;
import com.fermine.umweltlite.engine.body.systems.MotorSystem;
import com.fermine.umweltlite.engine.body.systems.SurvivalSystem;
import com.fermine.umweltlite.engine.body.systems.VisionSystem;
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

    public void setupBody(Object rawModel) {
        this.animationProvider = rawModel;

        systems.add(new MotorSystem());
        systems.add(new SurvivalSystem());
        systems.add(new VisionSystem());

        systems.sort(Comparator.comparing(IBodySystem::getSystemPriority));
        this.initialized = true;
    }

    public void tick(Mob mob, UmweltEngine engine, StorageRetrieval snapshot, StorageInsert intent) {
        if (!initialized) return;

        // 1. THE PULSE PHASE: Biological side effects & head tracking
        for (IBodySystem system : systems) {
            system.pulse(mob, engine, snapshot);
        }

        // 2. THE VECTOR PHASE: Execute the Intent
        // Start with the Master Intent's drive vector (e.g., from Pathfinding/Desires)
        Vec3 totalDrive = intent.driveVector();

        // 3. PHYSICAL CONSTRAINTS: Systems modify the drive
        // No longer passing 'centerRay' here—systems pull what they need from Intake
        for (IBodySystem system : systems) {
            totalDrive = totalDrive.add(system.getDriveVector(mob, engine, snapshot));
        }

        // 4. FINAL EXECUTION & SAFETY
        handleMovement(mob, totalDrive, intent);

        // Handle Animations
        intent.animation().ifPresent(animName -> {
            // Placeholder for GeckoLib/Citadel logic
        });
    }

    private void handleMovement(Mob mob, Vec3 drive, StorageInsert intent) {
        // Jump Urge handling
        if (intent.jumpUrge() > 0.5f && mob.onGround()) {
            mob.jumpFromGround();
        }

        // NaN & Inf Firewall: Ensure movement is physically possible
        if (drive.lengthSqr() > 0.001 && Double.isFinite(drive.x + drive.z)) {

            // 1. Smooth Rotation Logic
            float targetYaw = (float) (Mth.atan2(drive.z, drive.x) * (180 / Math.PI)) - 90.0F;
            mob.setYRot(Mth.approachDegrees(mob.getYRot(), targetYaw, 15.0F));
            mob.yBodyRot = mob.getYRot();
            mob.yHeadRot = Mth.approachDegrees(mob.yHeadRot, targetYaw, 20.0F);

            // 2. Navigation Execution
            // We use a look-ahead target to let the Vanilla Navigator handle pathing/stairs
            Vec3 targetPos = mob.position().add(drive.normalize().scale(1.5));
            mob.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0D);

        } else if (mob.getNavigation().isInProgress()) {
            // Stop if the drive is negligible
            mob.getNavigation().stop();
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