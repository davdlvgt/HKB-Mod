package de.davidvogt.hkbmod.network;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import de.davidvogt.hkbmod.research.Research;
import de.davidvogt.hkbmod.research.ResearchManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class StartResearchPacket {
    private final ResourceLocation researchId;

    public StartResearchPacket(ResourceLocation researchId) {
        this.researchId = researchId;
    }

    public static void encode(StartResearchPacket packet, FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.researchId);
    }

    public static StartResearchPacket decode(FriendlyByteBuf buf) {
        return new StartResearchPacket(buf.readResourceLocation());
    }

    public static void handle(StartResearchPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer serverPlayer = context.getSender();
            if (serverPlayer != null) {
                HkbMod.LOGGER.info("[StartResearchPacket] Received research start request: " + packet.researchId);

                // Check if player has research table menu open
                if (serverPlayer.containerMenu instanceof ResearchTableMenu menu) {
                    Research research = ResearchManager.getResearch(packet.researchId);
                    if (research != null) {
                        HkbMod.LOGGER.info("[StartResearchPacket] Starting research on server: " + research.getName());

                        // Validate research can be started (server-side security)
                        if (menu.isResearchAvailable(research) && menu.hasAllRequiredItemsInSlots(research)) {
                            menu.startResearch(research);

                            // Networking temporarily disabled
                        } else {
                            HkbMod.LOGGER.warn("[StartResearchPacket] Research not available or missing items: " + research.getName());
                        }
                    } else {
                        HkbMod.LOGGER.warn("[StartResearchPacket] Research not found: " + packet.researchId);
                    }
                } else {
                    HkbMod.LOGGER.warn("[StartResearchPacket] Player does not have research table menu open");
                }
            }
        });
        context.setPacketHandled(true);
    }
}