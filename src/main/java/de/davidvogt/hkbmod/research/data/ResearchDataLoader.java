package de.davidvogt.hkbmod.research.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.research.PlayerClass;
import de.davidvogt.hkbmod.research.Research;
import de.davidvogt.hkbmod.research.ResearchType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads research definitions from JSON files
 */
public class ResearchDataLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<ResourceLocation, Research> LOADED_RESEARCHES = new HashMap<>();

    /**
     * Load all research JSON files from resources
     */
    public static void loadResearches(ResourceManager resourceManager) {
        LOADED_RESEARCHES.clear();
        HkbMod.LOGGER.info("[ResearchDataLoader] Loading research definitions from JSON...");

        // Find all JSON files in the research directory
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
            "research",
            location -> location.getPath().endsWith(".json")
        );

        int loadedCount = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try {
                Research research = loadResearchFromJson(entry.getValue());
                if (research != null) {
                    LOADED_RESEARCHES.put(research.getId(), research);
                    loadedCount++;
                    HkbMod.LOGGER.info("[ResearchDataLoader] Loaded research: " + research.getId());
                }
            } catch (Exception e) {
                HkbMod.LOGGER.error("[ResearchDataLoader] Failed to load research from: " + entry.getKey(), e);
            }
        }

        HkbMod.LOGGER.info("[ResearchDataLoader] Successfully loaded " + loadedCount + " research definitions");
    }

    /**
     * Load a single research from JSON resource
     */
    private static Research loadResearchFromJson(Resource resource) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(resource.open())) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Parse basic properties
            ResourceLocation id = ResourceLocation.tryParse(json.get("id").getAsString());
            String name = json.get("name").getAsString();
            String description = json.get("description").getAsString();
            PlayerClass playerClass = PlayerClass.valueOf(json.get("player_class").getAsString());
            int tier = json.get("tier").getAsInt();
            ResearchType type = ResearchType.valueOf(json.get("type").getAsString());

            // Parse prerequisites
            List<ResourceLocation> prerequisites = new ArrayList<>();
            if (json.has("prerequisites")) {
                json.getAsJsonArray("prerequisites").forEach(element -> {
                    ResourceLocation prereq = ResourceLocation.tryParse(element.getAsString());
                    if (prereq != null) {
                        prerequisites.add(prereq);
                    }
                });
            }

            // Parse costs
            List<ItemStack> costs = new ArrayList<>();
            if (json.has("costs")) {
                json.getAsJsonArray("costs").forEach(element -> {
                    JsonObject costObj = element.getAsJsonObject();
                    String itemId = costObj.get("item").getAsString();
                    int count = costObj.get("count").getAsInt();

                    var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemId));
                    if (item != null && item != Items.AIR) {
                        costs.add(new ItemStack(item, count));
                    } else {
                        HkbMod.LOGGER.warn("[ResearchDataLoader] Unknown item in research cost: " + itemId);
                    }
                });
            }

            // Build the research object
            Research.Builder builder = new Research.Builder(id, name, playerClass)
                .description(description)
                .type(type)
                .tier(tier);

            // Add prerequisites
            for (ResourceLocation prereq : prerequisites) {
                builder.addPrerequisite(prereq);
            }

            // Add costs
            for (ItemStack cost : costs) {
                builder.addCost(cost);
            }

            return builder.build();

        } catch (Exception e) {
            HkbMod.LOGGER.error("[ResearchDataLoader] Error parsing research JSON", e);
            return null;
        }
    }

    /**
     * Get all loaded researches
     */
    public static Map<ResourceLocation, Research> getLoadedResearches() {
        return new HashMap<>(LOADED_RESEARCHES);
    }

    /**
     * Get a specific research by ID
     */
    public static Research getResearch(ResourceLocation id) {
        return LOADED_RESEARCHES.get(id);
    }

    /**
     * Check if researches have been loaded
     */
    public static boolean hasLoadedResearches() {
        return !LOADED_RESEARCHES.isEmpty();
    }
}