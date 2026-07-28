package dev.spoon.event;

import net.minecraft.entity.Entity;

public final class VelocityEvent {

    private final Entity entity;
    private final VelocitySource source;

    private double motionX;
    private double motionY;
    private double motionZ;

    public VelocityEvent(
            Entity entity,
            double motionX,
            double motionY,
            double motionZ,
            VelocitySource source
    ) {
        this.entity = entity;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.source = source;
    }

    public Entity getEntity() {
        return entity;
    }

    public VelocitySource getSource() {
        return source;
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