package de.davidvogt.hkbmod.entity.client;

import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.entity.custom.DeerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class DeerRenderer extends MobRenderer<DeerEntity, LivingEntityRenderState, DeerModel> {

    public DeerRenderer(EntityRendererProvider.Context context) {
        super(context, new DeerModel(context.bakeLayer(ModModelLayers.DEER_LAYER)), 0.6f);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState state) {
        return ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "textures/entity/deer.png");
    }
}