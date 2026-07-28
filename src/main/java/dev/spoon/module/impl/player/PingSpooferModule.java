package dev.spoon.module.impl.player;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import dev.spoon.setting.BooleanSetting;
import dev.spoon.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.C00PacketKeepAlive;

public final class PingSpooferModule extends Module {

    private static final double MAX_PING_MS = 1000.0D;

    /*
     * Used when "Use Range" is disabled.
     *
     * Add To Real Ping enabled:
     *     This is added delay.
     *
     * Add To Real Ping disabled:
     *     This is the desired total ping.
     */
    private final NumberSetting pingMs = registerSetting(
            new NumberSetting(
                    "Ping (ms)",
                    200.0D,
                    0.0D,
                    MAX_PING_MS,
                    25.0D
            )
    );

    private final BooleanSetting useRange = registerSetting(
            new BooleanSetting(
                    "Use Range",
                    false
            )
    );

    private final NumberSetting minimumPing = registerSetting(
            new NumberSetting(
                    "Min. Ping",
                    200.0D,
                    0.0D,
                    MAX_PING_MS,
                    25.0D
            )
    );

    private final NumberSetting maximumPing = registerSetting(
            new NumberSetting(
                    "Max. Ping",
                    500.0D,
                    0.0D,
                    MAX_PING_MS,
                    25.0D
            )
    );

    /*
     * true:
     *     resulting ping approximately equals real ping + selected value
     *
     * false:
     *     resulting ping approximately equals selected value
     */
    private final BooleanSetting addToRealPing = registerSetting(
            new BooleanSetting(
                    "Additive",
                    true
            )
    );

    private final Deque<DelayedKeepAlive> delayedPackets =
            new ArrayDeque<DelayedKeepAlive>();

    /*
     * Captured before the first delayed keep-alive. This must remain stable
     * while the module is enabled; otherwise replacement mode would oscillate
     * between simulated and real ping values.
     */
    private int baselinePingMs = -1;

    /*
     * Prevents random delays from causing keep-alive responses to be sent
     * out of order.
     */
    private long lastQueuedReleaseTime;

    private long lastSelectedPingMs;
    private long lastAppliedDelayMs;

    public PingSpooferModule() {
        super(
                "Ping Spoofer",
                "Spoofs ping responses (UNRELIABLE W/ GOOD AC)",
                ModuleCategory.PLAYER,
                Module.UNBOUND_KEY
        );
    }

    /**
     * @return true if the packet was queued and must not be sent immediately.
     */
    public boolean queueKeepAlive(C00PacketKeepAlive packet) {
        if (packet == null || !isEnabled()) {
            return false;
        }

        if (mc.theWorld == null
                || mc.thePlayer == null
                || mc.getNetHandler() == null
                || !mc.isSingleplayer()) {
            return false;
        }

        captureBaselinePing();

        long selectedPing = selectConfiguredPing();
        long appliedDelay = calculateAppliedDelay(selectedPing);

        lastSelectedPingMs = selectedPing;
        lastAppliedDelayMs = appliedDelay;

        if (appliedDelay <= 0L) {
            return false;
        }

        long currentTime = Minecraft.getSystemTime();
        long releaseTime = currentTime + appliedDelay;

        /*
         * Preserve packet ordering even if range mode selects a much smaller
         * delay for a later packet.
         */
        if (releaseTime < lastQueuedReleaseTime) {
            releaseTime = lastQueuedReleaseTime;
        }

        lastQueuedReleaseTime = releaseTime;

        delayedPackets.addLast(
                new DelayedKeepAlive(
                        packet,
                        releaseTime
                )
        );

        return true;
    }

    @Override
    public void onTick() {
        flushReadyPackets();
    }

    @Override
    protected void onEnable() {
        delayedPackets.clear();

        baselinePingMs = -1;
        lastQueuedReleaseTime = 0L;
        lastSelectedPingMs = 0L;
        lastAppliedDelayMs = 0L;

        /*
         * Capture immediately when possible so the displayed player-list ping
         * still represents the unmodified connection.
         */
        captureBaselinePing();
    }

    @Override
    protected void onDisable() {
        /*
         * Do not discard keep-alive responses. Send queued responses
         * immediately when the simulator is disabled.
         */
        flushAllPackets();

        baselinePingMs = -1;
        lastQueuedReleaseTime = 0L;
        lastSelectedPingMs = 0L;
        lastAppliedDelayMs = 0L;
    }

    private void captureBaselinePing() {
        if (baselinePingMs >= 0) {
            return;
        }

        /*
         * Fall back to zero when player-list information is not available.
         * This is normally sufficiently accurate for an integrated server.
         */
        baselinePingMs = 0;

        if (mc.thePlayer == null || mc.getNetHandler() == null) {
            return;
        }

        NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(
                mc.thePlayer.getUniqueID()
        );

        if (playerInfo != null) {
            baselinePingMs = Math.max(
                    0,
                    playerInfo.getResponseTime()
            );
        }
    }

    private long selectConfiguredPing() {
        if (!useRange.isEnabled()) {
            return Math.round(pingMs.getDoubleValue());
        }

        double first = minimumPing.getDoubleValue();
        double second = maximumPing.getDoubleValue();

        double minimum = Math.min(first, second);
        double maximum = Math.max(first, second);

        if (maximum <= minimum) {
            return Math.round(minimum);
        }

        return Math.round(
                minimum + Math.random() * (maximum - minimum)
        );
    }

    private long calculateAppliedDelay(long selectedPing) {
        if (addToRealPing.isEnabled()) {
            /*
             * Additive mode:
             *
             * total ping ~= baseline + selectedPing
             */
            return Math.max(0L, selectedPing);
        }

        /*
         * Replacement mode:
         *
         * total ping ~= baseline + appliedDelay
         * appliedDelay = target - baseline
         */
        return Math.max(
                0L,
                selectedPing - (long)Math.max(0, baselinePingMs)
        );
    }

    private void flushReadyPackets() {
        if (delayedPackets.isEmpty()) {
            return;
        }

        if (mc.getNetHandler() == null) {
            clearQueue();
            return;
        }

        long currentTime = Minecraft.getSystemTime();

        while (!delayedPackets.isEmpty()) {
            DelayedKeepAlive delayed = delayedPackets.peekFirst();

            if (delayed.releaseTime > currentTime) {
                break;
            }

            delayedPackets.removeFirst();
            mc.getNetHandler().addToSendQueue(delayed.packet);
        }

        if (delayedPackets.isEmpty()) {
            lastQueuedReleaseTime = 0L;
        }
    }

    private void flushAllPackets() {
        if (mc.getNetHandler() == null) {
            clearQueue();
            return;
        }

        while (!delayedPackets.isEmpty()) {
            DelayedKeepAlive delayed =
                    delayedPackets.removeFirst();

            mc.getNetHandler().addToSendQueue(delayed.packet);
        }

        lastQueuedReleaseTime = 0L;
    }

    private void clearQueue() {
        delayedPackets.clear();
        lastQueuedReleaseTime = 0L;
    }

    public int getBaselinePingMs() {
        return Math.max(0, baselinePingMs);
    }

    public long getLastSelectedPingMs() {
        return lastSelectedPingMs;
    }

    public long getLastAppliedDelayMs() {
        return lastAppliedDelayMs;
    }

    public long getEstimatedResultingPingMs() {
        return (long)getBaselinePingMs() + lastAppliedDelayMs;
    }

    public int getQueuedPacketCount() {
        return delayedPackets.size();
    }

    public boolean isUsingRange() {
        return useRange.isEnabled();
    }

    public boolean isAdditive() {
        return addToRealPing.isEnabled();
    }

    private static final class DelayedKeepAlive {

        private final C00PacketKeepAlive packet;
        private final long releaseTime;

        private DelayedKeepAlive(
                C00PacketKeepAlive packet,
                long releaseTime
        ) {
            this.packet = packet;
            this.releaseTime = releaseTime;
        }
    }
}