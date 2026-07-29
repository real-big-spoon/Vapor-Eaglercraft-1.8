package dev.spoon.module.impl.movement;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.spoon.module.Module;
import dev.spoon.module.ModuleCategory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class PauseNetworkModule extends Module {

    /*
     * Normal movement is sent approximately once per client tick.
     * This allows roughly one minute of queued movement at 20 TPS.
     */
    private static final int MAX_QUEUED_PACKETS = 1200;

    private final Deque<C03PacketPlayer> queuedPackets =
            new ArrayDeque<C03PacketPlayer>();

    /*
     * Prevents packets from one world or connection from being sent into
     * another connection.
     */
    private NetHandlerPlayClient queuedConnection;

    public PauseNetworkModule() {
        super(
                "Blink",
                "Queues movement updates until disabled",
                ModuleCategory.MOVEMENT,
                Module.UNBOUND_KEY
        );
    }

    /**
     * Called directly from EntityPlayerSP when it generates a movement packet.
     *
     * @return true when the packet was queued and must not be sent now.
     */
    public boolean queueMovementPacket(
            NetHandlerPlayClient connection,
            C03PacketPlayer packet
    ) {
        if (!isEnabled()) {
            return false;
        }

        if (connection == null || packet == null) {
            return false;
        }

        if (!isValidIntegratedConnection(connection)) {
            return false;
        }

        /*
         * Never combine packets belonging to different connections.
         */
        if (queuedConnection != null
                && queuedConnection != connection) {

            clearQueue();
        }

        queuedConnection = connection;

        /*
         * Avoid unbounded memory use. Resume and flush automatically if the
         * queue reaches the safety limit.
         */
        if (queuedPackets.size() >= MAX_QUEUED_PACKETS) {
            setEnabled(false);

            /*
             * The current packet was not queued, so the caller should send it
             * normally after the previous queue has been flushed.
             */
            return false;
        }

        queuedPackets.addLast(packet);
        return true;
    }

    @Override
    protected void onEnable() {
        /*
         * Starting a new pause must not retain packets from an earlier pause.
         */
        clearQueue();
    }

    @Override
    protected void onDisable() {
        /*
         * Disabling the module is the resume action.
         */
        flushQueuedPackets();
    }

    @Override
    public void onTick() {
        /*
         * Discard packets if the player leaves the world or changes
         * connections while the module is active.
         */
        if (queuedConnection != null
                && !isValidIntegratedConnection(queuedConnection)) {

            clearQueue();
        }
    }

    public void flushQueuedPackets() {
        if (queuedPackets.isEmpty()) {
            queuedConnection = null;
            return;
        }

        NetHandlerPlayClient connection = queuedConnection;

        if (!isValidIntegratedConnection(connection)) {
            clearQueue();
            return;
        }

        /*
         * These packets go directly through NetHandlerPlayClient. They cannot
         * be requeued because interception now occurs only in EntityPlayerSP's
         * movement helper.
         */
        while (!queuedPackets.isEmpty()) {
            C03PacketPlayer packet =
                    queuedPackets.removeFirst();

            connection.addToSendQueue(packet);
        }

        queuedConnection = null;
    }

    public void discardQueuedPackets() {
        clearQueue();
    }

    public int getQueuedPacketCount() {
        return queuedPackets.size();
    }

    private boolean isValidIntegratedConnection(
            NetHandlerPlayClient connection
    ) {
        return connection != null
                && mc.thePlayer != null
                && mc.theWorld != null;
    }

    private void clearQueue() {
        queuedPackets.clear();
        queuedConnection = null;
    }
}