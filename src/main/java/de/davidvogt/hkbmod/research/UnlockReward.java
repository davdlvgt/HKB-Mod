package de.davidvogt.hkbmod.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class UnlockReward {
    public enum Type {
        RECIPE,
        ABILITY,
        ITEM,
        FLAG
    }

    private final Type type;
    private final ResourceLocation targetId;
    private final String script; // Optional custom script for complex unlocks

    public UnlockReward(Type type, ResourceLocation targetId) {
        this(type, targetId, null);
    }

    public UnlockReward(Type type, ResourceLocation targetId, String script) {
        this.type = type;
        this.targetId = targetId;
        this.script = script;
    }

    public Type getType() { return type; }
    public ResourceLocation getTargetId() { return targetId; }
    public String getScript() { return script; }
    public boolean hasScript() { return script != null && !script.isEmpty(); }

    // NBT serialization
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type.name());
        tag.putString("targetId", targetId.toString());
        if (script != null) {
            tag.putString("script", script);
        }
        return tag;
    }

    public static UnlockReward loadFromNBT(CompoundTag tag) {
        Type type = Type.valueOf(tag.getString("type").orElse("FLAG"));
        ResourceLocation targetId = ResourceLocation.parse(tag.getString("targetId").orElse("unknown"));
        String script = tag.contains("script") ? tag.getString("script").orElse(null) : null;
        return new UnlockReward(type, targetId, script);
    }

    @Override
    public String toString() {
        return "UnlockReward{" + "type=" + type + ", targetId=" + targetId + "}";
    }
}