package dev.spoon.event;

import net.minecraft.entity.Entity;

public final class VelocityEvent {

    private final Entity entity;

    private double motionX;
    private double motionY;
    private double motionZ;

    public VelocityEvent(
            Entity entity,
            double motionX,
            double motionY,
            double motionZ
    ) {
        this.entity = entity;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public Entity getEntity() {
        return entity;
    }

    public double getMotionX() {
        return motionX;
    }

    public void setMotionX(double motionX) {
        this.motionX = motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public void setMotionY(double motionY) {
        this.motionY = motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    public void setMotionZ(double motionZ) {
        this.motionZ = motionZ;
    }
}