package de.davidvogt.hkbmod.entity.client;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class DeerModel extends EntityModel<LivingEntityRenderState> {
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public DeerModel(ModelPart root) {
        super(root, RenderType::entityCutoutNoCull);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Head (smaller than cow, with ears)
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 4.0F, -8.0F));

        // Ears
        head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-2.0F, -3.0F, 0.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -2.0F, -2.0F, 0.0F, -0.5236F, 0.0F));

        head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(28, 5).addBox(-1.0F, -3.0F, 0.0F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -2.0F, -2.0F, 0.0F, 0.5236F, 0.0F));

        // Small antlers (optional)
        head.addOrReplaceChild("right_antler", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-1.0F, -6.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.5F, -1.0F, -1.0F));

        head.addOrReplaceChild("left_antler", CubeListBuilder.create()
                .texOffs(36, 5).addBox(0.0F, -6.0F, -1.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.5F, -1.0F, -1.0F));

        // Body (similar to cow but slimmer)
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(18, 14).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, 1.5708F, 0.0F, 0.0F));

        // Legs (thinner than cow legs)
        partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 12.0F, 7.0F));

        partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-2.0F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.0F, 12.0F, -5.0F));

        partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(3.0F, 12.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        // Walking animation
        this.rightHindLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;
        this.leftHindLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float)Math.PI) * 1.4F * state.walkAnimationSpeed;
        this.rightFrontLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F + (float)Math.PI) * 1.4F * state.walkAnimationSpeed;
        this.leftFrontLeg.xRot = Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed;

        // Head movement
        this.head.xRot = state.xRot * ((float)Math.PI / 180F);
        this.head.yRot = state.yRot * ((float)Math.PI / 180F);
    }
}