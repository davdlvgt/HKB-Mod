package de.davidvogt.hkbmod.network;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.menu.ResearchTableMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class ResearchProgressSyncPacket {
    private final ResourceLocation researchId;
    private final boolean isResearching;
    private final int progress;
    private final int duration;

    public ResearchProgressSyncPacket(ResourceLocation researchId, boolean isResearching, int progress, int duration) {
        this.researchId = researchId;
        this.isResearching = isResearching;
        this.progress = progress;
        this.duration = duration;
    }

    public static void encode(ResearchProgressSyncPacket packet, FriendlyByteBuf buf) {
        // Write research ID (nullable)
        buf.writeBoolean(packet.researchId != null);
        if (packet.researchId != null) {
            buf.writeResourceLocation(packet.researchId);
        }

        buf.writeBoolean(packet.isResearching);
        buf.writeInt(packet.progress);
        buf.writeInt(packet.duration);
    }

    public static ResearchProgressSyncPacket decode(FriendlyByteBuf buf) {
        ResourceLocation researchId = null;
        if (buf.readBoolean()) {
            researchId = buf.readResourceLocation();
        }

        boolean isResearching = buf.readBoolean();
        int progress = buf.readInt();
        int duration = buf.readInt();

        return new ResearchProgressSyncPacket(researchId, isResearching, progress, duration);
    }

    public static void handle(ResearchProgressSyncPacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            // This packet is sent to client, so handle on client side
            if (FMLEnvironment.dist.isClient()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.player != null && minecraft.player.containerMenu instanceof ResearchTableMenu menu) {
                    HkbMod.LOGGER.info("[ResearchProgressSyncPacket] Received progress sync - Research: " +
                            (packet.researchId != null ? packet.researchId : "none") +
                            ", Researching: " + packet.isResearching +
                            ", Progress: " + packet.progress + "/" + packet.duration);

                    // Update client-side research state
                    if (menu.getBlockEntity() != null) {
                        if (packet.isResearching && packet.researchId != null) {
                            // Update research progress on client
                            menu.getBlockEntity().setResearchProgress(packet.progress, packet.duration, packet.researchId);
                        } else {
                            // Research was cancelled or completed
                            menu.getBlockEntity().clearClientResearchState();
                        }
                    }
                } else {
                    HkbMod.LOGGER.warn("[ResearchProgressSyncPacket] Player does not have research table menu open");
                }
            }
        });
        context.setPacketHandled(true);
    }
}