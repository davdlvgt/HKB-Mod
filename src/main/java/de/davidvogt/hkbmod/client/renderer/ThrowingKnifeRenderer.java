package de.davidvogt.hkbmod.client.renderer;

import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;

public class ThrowingKnifeRenderer extends ThrownItemRenderer<ThrowingKnifeEntity> {

    public ThrowingKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
}
