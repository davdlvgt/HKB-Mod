package de.davidvogt.hkbmod.research;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class Research {
    private final ResourceLocation id;
    private final String name;
    private final String description;
    private final ResearchType type;
    private final PlayerClass requiredClass;
    private final List<ItemStack> costs;
    private final Set<ResourceLocation> prerequisites;
    private final ResourceLocation iconLocation;
    private final int tier;
    private final boolean isClassUnlock;

    private Research(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.type = builder.type;
        this.requiredClass = builder.requiredClass;
        this.costs = Collections.unmodifiableList(new ArrayList<>(builder.costs));
        this.prerequisites = Collections.unmodifiableSet(new HashSet<>(builder.prerequisites));
        this.iconLocation = builder.iconLocation;
        this.tier = builder.tier;
        this.isClassUnlock = builder.isClassUnlock;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ResearchType getType() {
        return type;
    }

    public PlayerClass getRequiredClass() {
        return requiredClass;
    }

    public List<ItemStack> getCosts() {
        return costs;
    }

    public Set<ResourceLocation> getPrerequisites() {
        return prerequisites;
    }

    public ResourceLocation getIconLocation() {
        return iconLocation;
    }

    public int getTier() {
        return tier;
    }

    public boolean isClassUnlock() {
        return isClassUnlock;
    }

    public MutableComponent getDisplayName() {
        return Component.literal(name).withStyle(type.getColor());
    }

    public MutableComponent getDescriptionComponent() {
        return Component.literal(description);
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    public boolean hasCosts() {
        return !costs.isEmpty();
    }

    public static class Builder {
        private final ResourceLocation id;
        private final String name;
        private String description = "";
        private ResearchType type = ResearchType.UTILITY;
        private final PlayerClass requiredClass;
        private final List<ItemStack> costs = new ArrayList<>();
        private final Set<ResourceLocation> prerequisites = new HashSet<>();
        private ResourceLocation iconLocation;
        private int tier = 1;
        private boolean isClassUnlock = false;

        public Builder(ResourceLocation id, String name, PlayerClass requiredClass) {
            this.id = id;
            this.name = name;
            this.requiredClass = requiredClass;
            this.iconLocation = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                "textures/gui/research/" + id.getPath() + ".png");
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder type(ResearchType type) {
            this.type = type;
            return this;
        }

        public Builder addCost(ItemStack cost) {
            this.costs.add(cost);
            return this;
        }

        public Builder addPrerequisite(ResourceLocation prerequisite) {
            this.prerequisites.add(prerequisite);
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder iconLocation(ResourceLocation iconLocation) {
            this.iconLocation = iconLocation;
            return this;
        }

        public Builder classUnlock() {
            this.isClassUnlock = true;
            return this;
        }

        public Research build() {
            Objects.requireNonNull(id, "Research ID cannot be null");
            Objects.requireNonNull(name, "Research name cannot be null");
            Objects.requireNonNull(requiredClass, "Required class cannot be null");
            Objects.requireNonNull(iconLocation, "Icon location cannot be null");

            return new Research(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Research research = (Research) o;
        return Objects.equals(id, research.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Research{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", class=" + requiredClass +
                ", tier=" + tier +
                '}';
    }
}