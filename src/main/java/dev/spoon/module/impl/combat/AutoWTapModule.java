package dev.spoon.module.impl.combat;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public final class AutoWTapModule extends Module {

    /*
     * The player tick on which sprint should be restored.
     * -1 means no restart is pending.
     */
    private int resprintAtTick = -1;

    public AutoWTapModule() {
        super(
                "Auto W-Tap",
                "Automatically restores sprint after sprint attacks",
                ModuleCategory.PLAYER,
                Module.UNBOUND_KEY
        );
    }

    /**
     * Called immediately after an entity attack is initiated.
     *
     * wasSprinting must be captured before attackEntity() runs because
     * Minecraft normally disables sprint during a sprinting attack.
     */
    @Override
    public void onAttackEntity(
            Entity target,
            boolean wasSprinting
    ) {
        if (!wasSprinting) {
            return;
        }

        if (!isUsable()) {
            return;
        }

        /*
         * Ignore armor stands, items, boats, and other non-living entities.
         */
        if (!(target instanceof EntityLivingBase)) {
            return;
        }

        EntityLivingBase livingTarget =
                (EntityLivingBase)target;

        if (!livingTarget.isEntityAlive()) {
            return;
        }

        /*
         * ticksExisted prevents the sprint from being restored during the
         * same game tick as the attack. This gives vanilla enough time to
         * synchronize the stopped sprint state first.
         */
        resprintAtTick = mc.thePlayer.ticksExisted + 1;
    }

    @Override
    public void onTick() {
        if (resprintAtTick < 0) {
            return;
        }

        if (!isUsable()) {
            clearPendingResprint();
            return;
        }

        if (mc.thePlayer.ticksExisted < resprintAtTick) {
            return;
        }

        clearPendingResprint();

        if (!canResumeSprint()) {
            return;
        }

        mc.thePlayer.setSprinting(true);
    }

    @Override
    protected void onEnable() {
        clearPendingResprint();
    }

    @Override
    protected void onDisable() {
        clearPendingResprint();
    }

    private boolean isUsable() {
        return mc.thePlayer != null
                && mc.theWorld != null
                && mc.isSingleplayer();
    }

    private boolean canResumeSprint() {
        /*
         * moveForward is normally 1.0 while W is held.
         */
        if (mc.thePlayer.movementInput == null
                || mc.thePlayer.movementInput.moveForward < 0.8F) {
            return false;
        }

        if (mc.thePlayer.isSneaking()) {
            return false;
        }

        if (mc.thePlayer.isCollidedHorizontally) {
            return false;
        }

        /*
         * Match Minecraft's normal survival sprint requirement.
         * Creative players may sprint regardless of food level.
         */
        if (!mc.thePlayer.capabilities.allowFlying
                && mc.thePlayer.getFoodStats().getFoodLevel() <= 6) {
            return false;
        }

        return true;
    }

    private void clearPendingResprint() {
        resprintAtTick = -1;
    }
}