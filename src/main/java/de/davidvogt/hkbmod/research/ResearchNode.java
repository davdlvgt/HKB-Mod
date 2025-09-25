package de.davidvogt.hkbmod.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class ResearchNode {
    private final ResourceLocation id;
    private final String name;
    private final String description;
    private final ResearchClass researchClass;
    private final int tier;
    private final List<ItemCost> costs;
    private final List<ResourceLocation> prerequisites;
    private final List<ResourceLocation> crossRequisites;
    private final UnlockReward unlockReward;

    public ResearchNode(ResourceLocation id, String name, String description,
                       ResearchClass researchClass, int tier, List<ItemCost> costs,
                       List<ResourceLocation> prerequisites, List<ResourceLocation> crossRequisites,
                       UnlockReward unlockReward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.researchClass = researchClass;
        this.tier = tier;
        this.costs = costs != null ? costs : new ArrayList<>();
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.crossRequisites = crossRequisites != null ? crossRequisites : new ArrayList<>();
        this.unlockReward = unlockReward;
    }

    // Getters
    public ResourceLocation getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ResearchClass getResearchClass() { return researchClass; }
    public int getTier() { return tier; }
    public List<ItemCost> getCosts() { return costs; }
    public List<ResourceLocation> getPrerequisites() { return prerequisites; }
    public List<ResourceLocation> getCrossRequisites() { return crossRequisites; }
    public UnlockReward getUnlockReward() { return unlockReward; }

    // Helper methods
    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty() || !crossRequisites.isEmpty();
    }

    public int getTotalItemCost() {
        return costs.stream().mapToInt(ItemCost::getCount).sum();
    }

    // NBT serialization (disabled for now due to API changes)
    public CompoundTag saveToNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id.toString());
        tag.putString("name", name);
        tag.putString("description", description);
        tag.putString("class", researchClass.name());
        tag.putInt("tier", tier);

        // Save costs
        ListTag costsTag = new ListTag();
        for (ItemCost cost : costs) {
            costsTag.add(cost.saveToNBT());
        }
        tag.put("costs", costsTag);

        // Save prerequisites
        ListTag prereqTag = new ListTag();
        for (ResourceLocation prereq : prerequisites) {
            CompoundTag prereqCompound = new CompoundTag();
            prereqCompound.putString("id", prereq.toString());
            prereqTag.add(prereqCompound);
        }
        tag.put("prerequisites", prereqTag);

        // Save cross-requisites
        ListTag crossReqTag = new ListTag();
        for (ResourceLocation crossReq : crossRequisites) {
            CompoundTag crossReqCompound = new CompoundTag();
            crossReqCompound.putString("id", crossReq.toString());
            crossReqTag.add(crossReqCompound);
        }
        tag.put("crossRequisites", crossReqTag);

        // Save unlock reward
        if (unlockReward != null) {
            tag.put("unlockReward", unlockReward.saveToNBT());
        }

        return tag;
    }

    // Static factory method to load from NBT
    public static ResearchNode loadFromNBT(CompoundTag tag) {
        ResourceLocation id = ResourceLocation.parse(tag.getString("id").orElse("unknown"));
        String name = tag.getString("name").orElse("Unknown");
        String description = tag.getString("description").orElse("No description");
        ResearchClass researchClass = ResearchClass.valueOf(tag.getString("class").orElse("MAGICIAN"));
        int tier = tag.getInt("tier").orElse(0);

        // Load costs
        List<ItemCost> costs = new ArrayList<>();
        tag.getList("costs").ifPresent(costsTag -> {
            for (int i = 0; i < costsTag.size(); i++) {
                costsTag.getCompound(i).ifPresent(costTag ->
                    costs.add(ItemCost.loadFromNBT(costTag)));
            }
        });

        // Load prerequisites
        List<ResourceLocation> prerequisites = new ArrayList<>();
        tag.getList("prerequisites").ifPresent(prereqTag -> {
            for (int i = 0; i < prereqTag.size(); i++) {
                prereqTag.getCompound(i).ifPresent(prereqCompound ->
                    prereqCompound.getString("id").ifPresent(id2 ->
                        prerequisites.add(ResourceLocation.parse(id2))));
            }
        });

        // Load cross-requisites
        List<ResourceLocation> crossRequisites = new ArrayList<>();
        tag.getList("crossRequisites").ifPresent(crossReqTag -> {
            for (int i = 0; i < crossReqTag.size(); i++) {
                crossReqTag.getCompound(i).ifPresent(crossReqCompound ->
                    crossReqCompound.getString("id").ifPresent(id3 ->
                        crossRequisites.add(ResourceLocation.parse(id3))));
            }
        });

        // Load unlock reward
        UnlockReward[] unlockRewardHolder = {null};
        if (tag.contains("unlockReward")) {
            tag.getCompound("unlockReward").ifPresent(rewardTag ->
                unlockRewardHolder[0] = UnlockReward.loadFromNBT(rewardTag));
        }
        UnlockReward unlockReward = unlockRewardHolder[0];

        return new ResearchNode(id, name, description, researchClass, tier, costs,
                               prerequisites, crossRequisites, unlockReward);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ResearchNode that = (ResearchNode) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ResearchNode{" + "id=" + id + ", name='" + name + "', tier=" + tier + "}";
    }
}