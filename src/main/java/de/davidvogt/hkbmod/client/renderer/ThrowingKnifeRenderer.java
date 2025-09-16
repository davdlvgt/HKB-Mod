package de.davidvogt.hkbmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.davidvogt.hkbmod.entities.ThrowingKnifeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.world.phys.Vec3;

public class ThrowingKnifeRenderer extends ThrownItemRenderer<ThrowingKnifeEntity> {

    private float lastSpin = 0.0F;
    private float yaw = 0.0F;
    private float pitch = 0.0F;

    public ThrowingKnifeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void extractRenderState(ThrowingKnifeEntity entity, ThrownItemRenderState reusedState, float partialTicks) {
        super.extractRenderState(entity, reusedState, partialTicks);

        // Ausrichtung an Flugrichtung berechnen
        Vec3 motion = entity.getDeltaMovement();
        double dx = motion.x;
        double dy = motion.y;
        double dz = motion.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);

        // Yaw: Richtung in der XZ-Ebene, Pitch: Auf-/Abwärtsneigung
        this.yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        this.pitch = (float) Math.toDegrees(Math.atan2(dy, horiz));

        // Spin-Geschwindigkeit: höherer Basiswert + Multiplikator basierend auf Bewegung
        float baseSpinSpeed = 60.0F; // Grunddrehung (Grad pro Tick)
        float speed = (float) motion.length();
        float spinMultiplier = 1.0F + speed; // anpassen falls zu schnell/langsam
        this.lastSpin = (entity.tickCount + partialTicks) * baseSpinSpeed * spinMultiplier;
    }

    @Override
    public void render(ThrownItemRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Erst an Flugrichtung ausrichten (Yaw / Pitch)
        poseStack.mulPose(Axis.YP.rotationDegrees(this.yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(this.pitch));

        // Dann um die Längsachse (lokale X) spinnen, Korrekturoffset für Textur einbauen
        poseStack.mulPose(Axis.XP.rotationDegrees(this.lastSpin - 90.0F));

        super.render(renderState, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}