package de.davidvogt.hkbmod.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class PlayerResearchData {
    private static final String NBT_PLAYER_CLASS = "PlayerClass";
    private static final String NBT_UNLOCKED_RESEARCHES = "UnlockedResearches";
    private static final String NBT_CLASS_UNLOCKED = "ClassUnlocked";

    private PlayerClass playerClass;
    private final Set<ResourceLocation> unlockedResearches;
    private boolean classUnlocked;

    public PlayerResearchData() {
        this.playerClass = PlayerClass.KNIGHT; // Default class
        this.unlockedResearches = new HashSet<>();
        this.classUnlocked = false;
    }

    public PlayerResearchData(PlayerClass playerClass) {
        this.playerClass = playerClass;
        this.unlockedResearches = new HashSet<>();
        this.classUnlocked = false;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public Set<ResourceLocation> getUnlockedResearches() {
        return new HashSet<>(unlockedResearches);
    }

    public boolean hasUnlockedResearch(ResourceLocation researchId) {
        return unlockedResearches.contains(researchId);
    }

    public void unlockResearch(ResourceLocation researchId) {
        unlockedResearches.add(researchId);
    }

    public void lockResearch(ResourceLocation researchId) {
        unlockedResearches.remove(researchId);
    }

    public boolean isClassUnlocked() {
        return classUnlocked;
    }

    public void setClassUnlocked(boolean classUnlocked) {
        this.classUnlocked = classUnlocked;
    }

    public int getUnlockedResearchCount() {
        return unlockedResearches.size();
    }

    public int getUnlockedResearchCountForClass(PlayerClass targetClass, ResearchTree researchTree) {
        return (int) unlockedResearches.stream()
                .map(researchTree::getResearch)
                .filter(research -> research != null && research.getRequiredClass() == targetClass)
                .count();
    }

    public void reset() {
        this.playerClass = PlayerClass.KNIGHT;
        this.unlockedResearches.clear();
        this.classUnlocked = false;
    }

    public void resetResearches() {
        this.unlockedResearches.clear();
    }

    public boolean canChangeClass() {
        return !classUnlocked || unlockedResearches.isEmpty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        tag.putString(NBT_PLAYER_CLASS, playerClass.getSerializedName());
        tag.putBoolean(NBT_CLASS_UNLOCKED, classUnlocked);

        ListTag researchList = new ListTag();
        for (ResourceLocation researchId : unlockedResearches) {
            researchList.add(StringTag.valueOf(researchId.toString()));
        }
        tag.put(NBT_UNLOCKED_RESEARCHES, researchList);

        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        // Use API patterns that work with Minecraft 1.21.7
        try {
            this.playerClass = PlayerClass.fromString(tag.getString(NBT_PLAYER_CLASS).orElse("knight"));
        } catch (Exception e) {
            this.playerClass = PlayerClass.KNIGHT;
        }

        try {
            this.classUnlocked = tag.getBoolean(NBT_CLASS_UNLOCKED).orElse(false);
        } catch (Exception e) {
            this.classUnlocked = false;
        }

        this.unlockedResearches.clear();
        try {
            ListTag researchList = tag.getList(NBT_UNLOCKED_RESEARCHES).orElse(new ListTag());
            for (int i = 0; i < researchList.size(); i++) {
                try {
                    String researchIdString = researchList.getString(i).orElse("");
                    if (!researchIdString.isEmpty()) {
                        ResourceLocation researchId = ResourceLocation.parse(researchIdString);
                        this.unlockedResearches.add(researchId);
                    }
                } catch (Exception e) {
                    // Invalid research entry, skip
                }
            }
        } catch (Exception e) {
            // No research list or invalid format
        }
    }

    public PlayerResearchData copy() {
        PlayerResearchData copy = new PlayerResearchData(this.playerClass);
        copy.unlockedResearches.addAll(this.unlockedResearches);
        copy.classUnlocked = this.classUnlocked;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerResearchData that = (PlayerResearchData) o;
        return classUnlocked == that.classUnlocked
               && playerClass == that.playerClass
               && unlockedResearches.equals(that.unlockedResearches);
    }

    @Override
    public int hashCode() {
        int result = playerClass.hashCode();
        result = 31 * result + unlockedResearches.hashCode();
        result = 31 * result + (classUnlocked ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlayerResearchData{" +
                "playerClass=" + playerClass +
                ", unlockedResearches=" + unlockedResearches.size() +
                ", classUnlocked=" + classUnlocked +
                '}';
    }
}