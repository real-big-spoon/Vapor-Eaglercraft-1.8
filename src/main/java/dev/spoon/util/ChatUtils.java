package dev.spoon.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public final class ChatUtils {

    private ChatUtils() {
    }

    public static void sendMessage(String message) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.thePlayer == null) {
            return;
        }

        mc.thePlayer.addChatMessage(
                new ChatComponentText(
                        EnumChatFormatting.DARK_GRAY + "["
                                + EnumChatFormatting.RED + "Vapor"
                                + EnumChatFormatting.DARK_GRAY + "] "
                                + EnumChatFormatting.GRAY + message
                )
        );
    }
}