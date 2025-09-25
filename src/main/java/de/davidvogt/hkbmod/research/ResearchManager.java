package de.davidvogt.hkbmod.research;

import com.google.gson.*;
import de.davidvogt.hkbmod.HkbMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.*;
import java.util.stream.Collectors;

public class ResearchManager {
    // Note: Simplified implementation without JSON reload listener for now
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ResearchManager INSTANCE;

    private Map<ResourceLocation, ResearchNode> allNodes = new HashMap<>();
    private Map<ResearchClass, List<ResearchNode>> nodesByClass = new EnumMap<>(ResearchClass.class);

    public ResearchManager() {
        INSTANCE = this;

        // Initialize class maps
        for (ResearchClass clazz : ResearchClass.values()) {
            nodesByClass.put(clazz, new ArrayList<>());
        }
    }

    public static ResearchManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResearchManager();
        }
        return INSTANCE;
    }

    public static void addReloadListeners(AddReloadListenerEvent event) {
        // Note: For now we'll initialize nodes in code rather than JSON
        getInstance().initializeDefaultNodes();
    }

    // Simplified apply method - JSON loading disabled for stability
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                        ResourceManager resourceManager, ProfilerFiller profiler) {
        allNodes.clear();
        nodesByClass.values().forEach(List::clear);

        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation nodeId = entry.getKey();

            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                ResearchNode node = parseResearchNode(nodeId, json);
                allNodes.put(nodeId, node);
                nodesByClass.get(node.getResearchClass()).add(node);
            } catch (Exception e) {
                HkbMod.LOGGER.error("Failed to parse research node {}: {}", nodeId, e.getMessage());
            }
        }

        // Sort nodes by tier within each class
        for (List<ResearchNode> nodes : nodesByClass.values()) {
            nodes.sort(Comparator.comparing(ResearchNode::getTier));
        }

        HkbMod.LOGGER.info("Loaded {} research nodes", allNodes.size());
    }

    private ResearchNode parseResearchNode(ResourceLocation id, JsonObject json) {
        String name = json.get("name").getAsString();
        String description = json.get("description").getAsString();
        ResearchClass researchClass = ResearchClass.fromId(json.get("class").getAsString());
        int tier = json.get("tier").getAsInt();

        // Parse costs
        List<ItemCost> costs = new ArrayList<>();
        if (json.has("cost")) {
            JsonArray costArray = json.getAsJsonArray("cost");
            for (JsonElement costElement : costArray) {
                JsonObject costObj = costElement.getAsJsonObject();
                ResourceLocation itemId = ResourceLocation.parse(costObj.get("item").getAsString());
                int count = costObj.get("count").getAsInt();
                costs.add(new ItemCost(itemId, count));
            }
        }

        // Parse prerequisites
        List<ResourceLocation> prerequisites = new ArrayList<>();
        if (json.has("prerequisites")) {
            JsonArray prereqArray = json.getAsJsonArray("prerequisites");
            for (JsonElement prereqElement : prereqArray) {
                prerequisites.add(ResourceLocation.parse(prereqElement.getAsString()));
            }
        }

        // Parse cross-requisites
        List<ResourceLocation> crossRequisites = new ArrayList<>();
        if (json.has("cross_requisites")) {
            JsonArray crossReqArray = json.getAsJsonArray("cross_requisites");
            for (JsonElement crossReqElement : crossReqArray) {
                crossRequisites.add(ResourceLocation.parse(crossReqElement.getAsString()));
            }
        }

        // Parse unlock reward
        UnlockReward unlockReward = null;
        if (json.has("unlock")) {
            JsonObject unlockObj = json.getAsJsonObject("unlock");
            UnlockReward.Type type = UnlockReward.Type.valueOf(unlockObj.get("type").getAsString().toUpperCase());
            ResourceLocation targetId = ResourceLocation.parse(unlockObj.get("recipe_id").getAsString());
            String script = unlockObj.has("script") ? unlockObj.get("script").getAsString() : null;
            unlockReward = new UnlockReward(type, targetId, script);
        }

        return new ResearchNode(id, name, description, researchClass, tier, costs,
                               prerequisites, crossRequisites, unlockReward);
    }

    // Public API methods
    public ResearchNode getNode(ResourceLocation id) {
        return allNodes.get(id);
    }

    public List<ResearchNode> getNodesForClass(ResearchClass researchClass) {
        return new ArrayList<>(nodesByClass.get(researchClass));
    }

    public List<ResearchNode> getAvailableNodes(ResearchClass researchClass, Set<ResourceLocation> unlockedNodes) {
        return nodesByClass.get(researchClass).stream()
                .filter(node -> !unlockedNodes.contains(node.getId()))
                .filter(node -> canUnlockNode(node, unlockedNodes))
                .collect(Collectors.toList());
    }

    public boolean canUnlockNode(ResearchNode node, Set<ResourceLocation> unlockedNodes) {
        // Check prerequisites
        for (ResourceLocation prereq : node.getPrerequisites()) {
            if (!unlockedNodes.contains(prereq)) {
                return false;
            }
        }

        // Check cross-requisites
        for (ResourceLocation crossReq : node.getCrossRequisites()) {
            if (!unlockedNodes.contains(crossReq)) {
                return false;
            }
        }

        return true;
    }

    public Collection<ResearchNode> getAllNodes() {
        return allNodes.values();
    }

    // Initialize default research nodes for development/testing
    public void initializeDefaultNodes() {
        if (!allNodes.isEmpty()) return; // Already loaded from JSON

        // Create some basic nodes for each class
        createMagicianNodes();
        createArcherNodes();
        createKnightNodes();
        createCavalierNodes();
    }

    private void createMagicianNodes() {
        // Basic Elemental (Tier 0)
        ResourceLocation basicElementalId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "basic_elemental");
        List<ItemCost> basicCosts = List.of(
                new ItemCost(Items.PAPER, 5),
                new ItemCost(Items.STICK, 1)
        );
        ResearchNode basicElemental = new ResearchNode(
                basicElementalId,
                "Basic Elemental",
                "Introduction to elemental magic",
                ResearchClass.MAGICIAN,
                0,
                basicCosts,
                List.of(),
                List.of(),
                new UnlockReward(UnlockReward.Type.FLAG, basicElementalId)
        );

        allNodes.put(basicElementalId, basicElemental);
        nodesByClass.get(ResearchClass.MAGICIAN).add(basicElemental);

        // Fire Wand (Tier 1)
        ResourceLocation fireWandId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "fire_wand");
        List<ItemCost> fireWandCosts = List.of(
                new ItemCost(Items.BLAZE_POWDER, 3),
                new ItemCost(Items.STICK, 1),
                new ItemCost(Items.PAPER, 5)
        );
        ResearchNode fireWand = new ResearchNode(
                fireWandId,
                "Fire Wand",
                "Unlocks crafting recipe for Fire Wand",
                ResearchClass.MAGICIAN,
                1,
                fireWandCosts,
                List.of(basicElementalId),
                List.of(),
                new UnlockReward(UnlockReward.Type.RECIPE, ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "fire_wand"))
        );

        allNodes.put(fireWandId, fireWand);
        nodesByClass.get(ResearchClass.MAGICIAN).add(fireWand);
    }

    private void createArcherNodes() {
        // Basic Marksmanship (Tier 0)
        ResourceLocation basicMarksmanshipId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "basic_marksmanship");
        List<ItemCost> basicCosts = List.of(
                new ItemCost(Items.STRING, 3),
                new ItemCost(Items.STICK, 2)
        );
        ResearchNode basicMarksmanship = new ResearchNode(
                basicMarksmanshipId,
                "Basic Marksmanship",
                "Introduction to ranged combat",
                ResearchClass.ARCHER,
                0,
                basicCosts,
                List.of(),
                List.of(),
                new UnlockReward(UnlockReward.Type.FLAG, basicMarksmanshipId)
        );

        allNodes.put(basicMarksmanshipId, basicMarksmanship);
        nodesByClass.get(ResearchClass.ARCHER).add(basicMarksmanship);
    }

    private void createKnightNodes() {
        // Basic Defense (Tier 0)
        ResourceLocation basicDefenseId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "basic_defense");
        List<ItemCost> basicCosts = List.of(
                new ItemCost(Items.IRON_INGOT, 2),
                new ItemCost(Items.LEATHER, 3)
        );
        ResearchNode basicDefense = new ResearchNode(
                basicDefenseId,
                "Basic Defense",
                "Introduction to heavy armor and shields",
                ResearchClass.KNIGHT,
                0,
                basicCosts,
                List.of(),
                List.of(),
                new UnlockReward(UnlockReward.Type.FLAG, basicDefenseId)
        );

        allNodes.put(basicDefenseId, basicDefense);
        nodesByClass.get(ResearchClass.KNIGHT).add(basicDefense);
    }

    private void createCavalierNodes() {
        // Mounted Basics (Tier 0)
        ResourceLocation mountedBasicsId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "mounted_basics");
        List<ItemCost> basicCosts = List.of(
                new ItemCost(Items.SADDLE, 1),
                new ItemCost(Items.LEATHER, 4)
        );
        ResearchNode mountedBasics = new ResearchNode(
                mountedBasicsId,
                "Mounted Basics",
                "Introduction to cavalry combat",
                ResearchClass.CAVALIER,
                0,
                basicCosts,
                List.of(),
                List.of(),
                new UnlockReward(UnlockReward.Type.FLAG, mountedBasicsId)
        );

        allNodes.put(mountedBasicsId, mountedBasics);
        nodesByClass.get(ResearchClass.CAVALIER).add(mountedBasics);
    }
}