package de.davidvogt.hkbmod.research;

import de.davidvogt.hkbmod.util.NBTUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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

    public PlayerClass getCurrentClass() {
        return playerClass;
    }

    public void setPlayerClass(PlayerClass playerClass) {
        this.playerClass = playerClass;
    }

    public Set<ResourceLocation> getUnlockedResearches() {
        return new HashSet<>(unlockedResearches);
    }

    public Set<ResourceLocation> getCompletedResearches() {
        return getUnlockedResearches();
    }

    public boolean hasUnlockedResearch(ResourceLocation researchId) {
        return unlockedResearches.contains(researchId);
    }

    public boolean hasCompletedResearch(ResourceLocation researchId) {
        return hasUnlockedResearch(researchId);
    }

    public boolean isResearchInProgress(ResourceLocation researchId) {
        // For now, no research is ever "in progress" - they're either completed or not
        // This could be extended to track research that's being worked on
        return false;
    }

    public boolean hasUnlockedClass(PlayerClass playerClass) {
        return playerClass == this.playerClass; // Current class is always "unlocked"
        // For other classes, check if player has completed class unlock researches
        // This is a simplified implementation
    }

    public void unlockResearch(ResourceLocation researchId) {
        unlockedResearches.add(researchId);
        // Automatisch speichern wird vom PlayerResearchDataManager übernommen
    }

    public void unlockResearch(ResourceLocation researchId, net.minecraft.world.entity.player.Player player) {
        unlockedResearches.add(researchId);
        // Automatisch speichern wenn Spieler verfügbar ist
        if (player != null) {
            PlayerResearchDataManager.savePlayerResearchData(player, this);
        }
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
        // Use NBTUtil for proper Optional handling
        if (tag.contains(NBT_PLAYER_CLASS)) {
            String classString = NBTUtil.getString(tag, NBT_PLAYER_CLASS);
            this.playerClass = PlayerClass.fromString(classString);
        } else {
            this.playerClass = PlayerClass.KNIGHT;
        }

        this.classUnlocked = NBTUtil.getBoolean(tag, NBT_CLASS_UNLOCKED);

        this.unlockedResearches.clear();
        if (tag.contains(NBT_UNLOCKED_RESEARCHES)) {
            ListTag researchList = NBTUtil.getList(tag, NBT_UNLOCKED_RESEARCHES);
            for (int i = 0; i < researchList.size(); i++) {
                try {
                    String researchIdString = NBTUtil.getStringFromList(researchList, i);
                    if (!researchIdString.isEmpty()) {
                        ResourceLocation researchId = ResourceLocation.parse(researchIdString);
                        this.unlockedResearches.add(researchId);
                    }
                } catch (Exception e) {
                    // Invalid research entry, skip
                }
            }
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

    private void addSampleResearches() {
        // Entfernt - Sample researches überschreiben geladene Daten
        // Beispiel-Forschungen werden nur noch zur Entwicklungszeit gesetzt
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