package de.davidvogt.hkbmod.entity.client;

import de.davidvogt.hkbmod.HkbMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation DEER_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "deer"), "main");
}