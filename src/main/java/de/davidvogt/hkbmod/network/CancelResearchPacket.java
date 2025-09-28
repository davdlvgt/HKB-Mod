package de.davidvogt.hkbmod.network;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class CancelResearchPacket {

    public CancelResearchPacket() {
        // Empty packet - no data needed
    }

    public static void encode(CancelResearchPacket packet, FriendlyByteBuf buf) {
        // No data to encode
    }

    public static CancelResearchPacket decode(FriendlyByteBuf buf) {
        return new CancelResearchPacket();
    }

    public static void handle(CancelResearchPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                HkbMod.LOGGER.info("[CancelResearchPacket] Received research cancel request from player: " + serverPlayer.getName().getString());

                // Check if player has research table menu open
                if (serverPlayer.containerMenu instanceof ResearchTableMenu menu) {
                    if (menu.getBlockEntity() != null && menu.getBlockEntity().isResearching()) {
                        HkbMod.LOGGER.info("[CancelResearchPacket] Cancelling research on server");
                        menu.getBlockEntity().cancelResearch();

                        // Networking temporarily disabled
                    } else {
                        HkbMod.LOGGER.warn("[CancelResearchPacket] No research in progress to cancel");
                    }
                } else {
                    HkbMod.LOGGER.warn("[CancelResearchPacket] Player does not have research table menu open");
                }
            }
        });
        context.setPacketHandled(true);
    }
}