package io.github.knap43.photomode;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A client-only, never-spawned entity the game renders from. Markers have zero size,
 * so the eye sits exactly at the entity's position.
 */
final class PhotoCamera extends Entity {
    PhotoCamera(Level level, Vec3 eye, float yaw, float pitch) {
        super(EntityType.MARKER, level);
        this.noPhysics = true;
        this.setPos(eye);
        this.setYRot(yaw);
        this.setXRot(pitch);
        settle();
    }

    /** Positions are driven per frame, so there is nothing to interpolate. */
    void settle() {
        this.xo = this.xOld = this.getX();
        this.yo = this.yOld = this.getY();
        this.zo = this.zOld = this.getZ();
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {}
}
