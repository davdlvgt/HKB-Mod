package de.davidvogt.hkbmod.research;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

public class ResearchManager {
    private static ResearchTree globalResearchTree;
    private static boolean initialized = false;

    public static void initialize() {
        if (!initialized) {
            globalResearchTree = new ResearchTree();
            ModResearches.populateResearchTree(globalResearchTree);

            // Add sample researches if none were loaded
            if (globalResearchTree.getTotalResearchCount() == 0) {
                addSampleResearches(globalResearchTree);
            }

            initialized = true;
        }
    }

    private static void addSampleResearches(ResearchTree tree) {
        // Tier 0 - Basic researches
        Research basicCombat = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_combat"),
            "Basic Combat",
            PlayerClass.KNIGHT
        ).description("Learn the fundamentals of combat").tier(0).type(ResearchType.COMBAT).build();

        Research basicArchery = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_archery"),
            "Basic Archery",
            PlayerClass.ARCHER
        ).description("Learn the fundamentals of archery").tier(0).type(ResearchType.COMBAT).build();

        Research basicMagic = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_magic"),
            "Basic Magic",
            PlayerClass.MAGICIAN
        ).description("Learn the fundamentals of magic").tier(0).type(ResearchType.MAGIC).build();

        Research basicRiding = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_riding"),
            "Basic Riding",
            PlayerClass.CAVALIER
        ).description("Learn the fundamentals of riding").tier(0).type(ResearchType.UTILITY).build();

        // Knight researches
        Research knightFoundation = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_foundation"),
            "Knight Foundation",
            PlayerClass.KNIGHT
        ).description("Basic knight training").tier(1).type(ResearchType.PASSIVE)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_combat"))
         .build();

        Research knightBasic = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_basic"),
            "Knight Combat",
            PlayerClass.KNIGHT
        ).description("Advanced knight combat").tier(2).type(ResearchType.COMBAT)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_foundation"))
         .build();

        // Additional tier 1 knight researches for testing
        Research knightDefense = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_defense"),
            "Shield Mastery",
            PlayerClass.KNIGHT
        ).description("Master shield techniques").tier(1).type(ResearchType.UTILITY)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_combat"))
         .build();

        Research knightStrength = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_strength"),
            "Power Training",
            PlayerClass.KNIGHT
        ).description("Increase physical strength").tier(1).type(ResearchType.PASSIVE)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_combat"))
         .build();

        // Additional knight research to test type ordering
        Research knightCrafting = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "knight_crafting"),
            "Weapon Forging",
            PlayerClass.KNIGHT
        ).description("Learn to craft weapons").tier(1).type(ResearchType.CRAFTING)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_combat"))
         .build();

        // Archer researches
        Research archerFoundation = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_foundation"),
            "Archer Foundation",
            PlayerClass.ARCHER
        ).description("Basic archer training").tier(1)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_archery"))
         .build();

        Research archerBasic = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_basic"),
            "Precision Shooting",
            PlayerClass.ARCHER
        ).description("Advanced archery skills").tier(2)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_foundation"))
         .build();

        // Additional tier 1 archer researches for testing
        Research archerSpeed = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_speed"),
            "Quick Draw",
            PlayerClass.ARCHER
        ).description("Faster bow drawing").tier(1).type(ResearchType.COMBAT)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_archery"))
         .build();

        Research archerMultishot = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_multishot"),
            "Multi-Shot",
            PlayerClass.ARCHER
        ).description("Fire multiple arrows").tier(1).type(ResearchType.UTILITY)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_archery"))
         .build();

        // Additional tier 1 archer research to test wider columns
        Research archerStealth = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_stealth"),
            "Stealth",
            PlayerClass.ARCHER
        ).description("Move silently").tier(1).type(ResearchType.PASSIVE)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_archery"))
         .build();

        // Additional archer research to test type ordering
        Research archerMagic = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_magic"),
            "Enchanted Arrows",
            PlayerClass.ARCHER
        ).description("Magical arrow enhancement").tier(1).type(ResearchType.MAGIC)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_archery"))
         .build();

        // Magician researches
        Research magicianFoundation = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_foundation"),
            "Magician Foundation",
            PlayerClass.MAGICIAN
        ).description("Basic magic training").tier(1)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_magic"))
         .build();

        Research magicianBasic = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_basic"),
            "Elemental Magic",
            PlayerClass.MAGICIAN
        ).description("Elemental spell casting").tier(2)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "magician_foundation"))
         .build();

        // Cavalier researches
        Research cavalierFoundation = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_foundation"),
            "Cavalier Foundation",
            PlayerClass.CAVALIER
        ).description("Basic cavalry training").tier(1)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "basic_riding"))
         .build();

        Research cavalierBasic = new Research.Builder(
            ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_basic"),
            "Mount Mastery",
            PlayerClass.CAVALIER
        ).description("Advanced riding skills").tier(2)
         .addPrerequisite(ResourceLocation.fromNamespaceAndPath("hkbmod", "cavalier_foundation"))
         .build();

        // Add to tree (including tier 0 researches and additional tier 1 researches)
        tree.addResearch(basicCombat);
        tree.addResearch(basicArchery);
        tree.addResearch(basicMagic);
        tree.addResearch(basicRiding);
        tree.addResearch(knightFoundation);
        tree.addResearch(knightDefense);
        tree.addResearch(knightStrength);
        tree.addResearch(knightCrafting);
        tree.addResearch(knightBasic);
        tree.addResearch(archerFoundation);
        tree.addResearch(archerSpeed);
        tree.addResearch(archerMultishot);
        tree.addResearch(archerStealth);
        tree.addResearch(archerMagic);
        tree.addResearch(archerBasic);
        tree.addResearch(magicianFoundation);
        tree.addResearch(magicianBasic);
        tree.addResearch(cavalierFoundation);
        tree.addResearch(cavalierBasic);
    }

    public static ResearchTree getGlobalResearchTree() {
        if (!initialized) {
            initialize();
        }
        return globalResearchTree;
    }

    public static Research getResearch(ResourceLocation id) {
        return getGlobalResearchTree().getResearch(id);
    }

    public static List<Research> getAvailableResearches(PlayerClass playerClass, PlayerResearchData playerData) {
        return getGlobalResearchTree().getAvailableResearches(playerClass, playerData.getUnlockedResearches());
    }

    public static List<Research> getResearchesForClass(PlayerClass playerClass) {
        return getGlobalResearchTree().getResearchesForClass(playerClass);
    }

    public static List<Research> getResearchesByTier(PlayerClass playerClass, int tier) {
        return getGlobalResearchTree().getResearchesByTier(playerClass, tier);
    }

    public static boolean canUnlockResearch(Research research, PlayerResearchData playerData, Player player) {
        if (research == null || playerData == null) {
            return false;
        }

        // Check if already unlocked
        if (playerData.hasUnlockedResearch(research.getId())) {
            return false;
        }

        // Check if player has the required class
        if (research.getRequiredClass() != playerData.getPlayerClass()) {
            return false;
        }

        // Check prerequisites
        if (!getGlobalResearchTree().arePrerequisitesMet(research, playerData.getUnlockedResearches())) {
            return false;
        }

        // Check if player has required items
        return hasRequiredItems(player, research.getCosts());
    }

    public static boolean unlockResearch(Research research, PlayerResearchData playerData, Player player) {
        if (!canUnlockResearch(research, playerData, player)) {
            return false;
        }

        // Consume required items
        if (!consumeItems(player, research.getCosts())) {
            return false;
        }

        // Unlock the research
        playerData.unlockResearch(research.getId());
        return true;
    }

    // Check if research is available to select and research
    public static boolean isResearchAvailable(Research research, PlayerResearchData playerData) {
        if (research == null || playerData == null) {
            return false;
        }

        // Check if already unlocked (completed research cannot be researched again)
        if (playerData.hasUnlockedResearch(research.getId())) {
            return false;
        }

        // Check if player has the required class
        if (research.getRequiredClass() != playerData.getPlayerClass()) {
            return false;
        }

        // Check prerequisites (Tier 0 has no prerequisites, so will be available immediately)
        // Higher tiers require previous tier research to be completed
        return getGlobalResearchTree().arePrerequisitesMet(research, playerData.getUnlockedResearches());
    }

    // New methods for slot-based research
    public static boolean canUnlockResearchWithItems(Research research, PlayerResearchData playerData, NonNullList<ItemStack> researchItems) {
        if (research == null || playerData == null) {
            System.out.println("[ResearchManager] DEBUG: research or playerData is null");
            return false;
        }

        // Check if player has the required class
        if (research.getRequiredClass() != playerData.getPlayerClass()) {
            System.out.println("[ResearchManager] DEBUG: " + research.getName() + " class mismatch - need " + research.getRequiredClass() + ", have " + playerData.getPlayerClass());
            return false;
        }

        // Check prerequisites (must be available to research)
        if (!isResearchAvailable(research, playerData)) {
            System.out.println("[ResearchManager] DEBUG: " + research.getName() + " not available for research");
            return false;
        }

        // Check if research slots have required items
        if (!hasRequiredItemsInSlots(researchItems, research.getCosts())) {
            System.out.println("[ResearchManager] DEBUG: " + research.getName() + " missing items - needs " + research.getCosts().size() + " items");
            return false;
        }

        System.out.println("[ResearchManager] DEBUG: " + research.getName() + " CAN be unlocked!");
        return true;
    }

    public static boolean unlockResearchWithItems(Research research, PlayerResearchData playerData, NonNullList<ItemStack> researchItems) {
        if (!canUnlockResearchWithItems(research, playerData, researchItems)) {
            return false;
        }

        // Consume required items from slots
        if (!consumeItemsFromSlots(researchItems, research.getCosts())) {
            return false;
        }

        // Unlock the research
        playerData.unlockResearch(research.getId());
        return true;
    }

    private static boolean hasRequiredItemsInSlots(NonNullList<ItemStack> researchItems, List<ItemStack> costs) {
        for (ItemStack cost : costs) {
            if (!hasItemInSlots(researchItems, cost)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasItemInSlots(NonNullList<ItemStack> researchItems, ItemStack requiredStack) {
        int requiredCount = requiredStack.getCount();
        int foundCount = 0;

        for (ItemStack stack : researchItems) {
            if (ItemStack.isSameItemSameComponents(stack, requiredStack)) {
                foundCount += stack.getCount();
                if (foundCount >= requiredCount) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean consumeItemsFromSlots(NonNullList<ItemStack> researchItems, List<ItemStack> costs) {
        // First, check if we have all items (safety check)
        if (!hasRequiredItemsInSlots(researchItems, costs)) {
            return false;
        }

        // Then consume them
        for (ItemStack cost : costs) {
            consumeItemFromSlots(researchItems, cost);
        }

        return true;
    }

    private static void consumeItemFromSlots(NonNullList<ItemStack> researchItems, ItemStack requiredStack) {
        int remainingToConsume = requiredStack.getCount();

        for (int i = 0; i < researchItems.size() && remainingToConsume > 0; i++) {
            ItemStack stack = researchItems.get(i);
            if (ItemStack.isSameItemSameComponents(stack, requiredStack)) {
                int toTake = Math.min(remainingToConsume, stack.getCount());
                stack.shrink(toTake);
                remainingToConsume -= toTake;

                if (stack.isEmpty()) {
                    researchItems.set(i, ItemStack.EMPTY);
                }
            }
        }
    }

    // Original player inventory methods
    private static boolean hasRequiredItems(Player player, List<ItemStack> costs) {
        for (ItemStack cost : costs) {
            if (!hasItemInInventory(player, cost)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasItemInInventory(Player player, ItemStack requiredStack) {
        int requiredCount = requiredStack.getCount();
        int foundCount = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, requiredStack)) {
                foundCount += stack.getCount();
                if (foundCount >= requiredCount) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean consumeItems(Player player, List<ItemStack> costs) {
        // First, check if we have all items (safety check)
        if (!hasRequiredItems(player, costs)) {
            return false;
        }

        // Then consume them
        for (ItemStack cost : costs) {
            consumeItemFromInventory(player, cost);
        }

        return true;
    }

    private static void consumeItemFromInventory(Player player, ItemStack requiredStack) {
        int remainingToConsume = requiredStack.getCount();

        for (int i = 0; i < player.getInventory().getContainerSize() && remainingToConsume > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, requiredStack)) {
                int toTake = Math.min(remainingToConsume, stack.getCount());
                stack.shrink(toTake);
                remainingToConsume -= toTake;

                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    public static int getMaxTierForClass(PlayerClass playerClass) {
        return getGlobalResearchTree().getMaxTierForClass(playerClass);
    }

    public static List<Research> getRootResearches(PlayerClass playerClass) {
        return getGlobalResearchTree().getRootResearches(playerClass);
    }

    public static List<Research> getDirectPrerequisites(Research research) {
        return getGlobalResearchTree().getDirectPrerequisites(research);
    }

    public static List<Research> getDirectDependents(Research research) {
        return getGlobalResearchTree().getDirectDependents(research);
    }

    public static List<Research> getResearchPath(Research target, Set<ResourceLocation> unlockedResearches) {
        return getGlobalResearchTree().getResearchPath(target, unlockedResearches);
    }

    public static int getTotalResearchCount() {
        return getGlobalResearchTree().getTotalResearchCount();
    }

    public static int getResearchCountForClass(PlayerClass playerClass) {
        return getGlobalResearchTree().getResearchCountForClass(playerClass);
    }

    // Utility method to get research progress for a class
    public static ResearchProgress getResearchProgress(PlayerClass playerClass, PlayerResearchData playerData) {
        int total = getResearchCountForClass(playerClass);
        int unlocked = playerData.getUnlockedResearchCountForClass(playerClass, getGlobalResearchTree());
        int available = getAvailableResearches(playerClass, playerData).size();

        return new ResearchProgress(total, unlocked, available);
    }

    public static class ResearchProgress {
        private final int total;
        private final int unlocked;
        private final int available;

        public ResearchProgress(int total, int unlocked, int available) {
            this.total = total;
            this.unlocked = unlocked;
            this.available = available;
        }

        public int getTotal() { return total; }
        public int getUnlocked() { return unlocked; }
        public int getAvailable() { return available; }
        public int getLocked() { return total - unlocked - available; }

        public float getUnlockedPercentage() {
            return total > 0 ? (float) unlocked / total : 0f;
        }

        public float getProgressPercentage() {
            return total > 0 ? (float) (unlocked + available) / total : 0f;
        }
    }
}