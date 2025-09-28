# Research System Documentation

This document provides comprehensive documentation for the Research Table system implemented in the HKB Mod.

## Overview

The Research Table is a class-based progression system that allows players to unlock new recipes, items, and abilities through research. Players can choose from four distinct classes, each with their own research trees and specializations.

## System Architecture

### Core Classes

#### ResearchClass Enum
```java
public enum ResearchClass {
    MAGICIAN("magician", "Magician", 0xFF4A90E2, "textures/gui/research/class_magician.png"),
    ARCHER("archer", "Archer", 0xFF7ED321, "textures/gui/research/class_archer.png"),
    KNIGHT("knight", "Knight", 0xFFD0021B, "textures/gui/research/class_knight.png"),
    CAVALIER("cavalier", "Cavalier", 0xFFB8860B, "textures/gui/research/class_cavalier.png");
}
```

Each class has:
- **ID**: Internal identifier
- **Display Name**: User-friendly name
- **Color**: Hex color for UI theming
- **Icon Path**: Texture location for class icons

#### ResearchNode Class
```java
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
}
```

**Properties:**
- **ID**: Unique identifier (e.g., `hkbmod:fire_wand`)
- **Name**: Display name shown in UI
- **Description**: Detailed description of what the research unlocks
- **Research Class**: Which class tree this belongs to
- **Tier**: Difficulty/progression level (0 = basic, higher = advanced)
- **Costs**: Required items to start research
- **Prerequisites**: Required research from same class
- **Cross-Requisites**: Required research from other classes
- **Unlock Reward**: What gets unlocked upon completion

#### ItemCost Class
```java
public class ItemCost {
    private final ResourceLocation itemId;
    private final int count;

    // Example usage:
    new ItemCost(Items.BLAZE_POWDER, 3)  // 3x Blaze Powder
}
```

#### UnlockReward Class
```java
public class UnlockReward {
    public enum Type {
        RECIPE,   // Unlocks a crafting recipe
        ABILITY,  // Unlocks a special ability
        ITEM,     // Gives an item directly
        FLAG      // Sets a boolean flag for other systems
    }

    private final Type type;
    private final ResourceLocation targetId;
    private final String script; // Optional custom unlock logic
}
```

### Block Entity System

#### ResearchTableBlockEntity
The core block entity that handles:
- **Inventory Management**: 16 slots total
  - Slots 0-8: 3x3 crafting grid
  - Slot 9: Crafting result
  - Slots 10-15: Research input materials
- **Research State**: Tracking active research, progress, unlocked nodes
- **NBT Persistence**: Save/load research progress to world data
- **Ticking**: Advance research progress over time

```java
public class ResearchTableBlockEntity extends BlockEntity implements MenuProvider {
    // Slot configuration
    public static final int CRAFTING_GRID_SIZE = 9;
    public static final int CRAFTING_RESULT_SLOT = 9;
    public static final int RESEARCH_INPUT_START = 10;
    public static final int RESEARCH_INPUT_SIZE = 6;
    public static final int TOTAL_SLOTS = 16;

    // Research state
    private Set<ResourceLocation> unlockedResearches = new HashSet<>();
    private ResourceLocation activeResearchId = null;
    private int researchProgress = 0;
    private int researchMaxProgress = 0;
    private boolean isResearching = false;
}
```

**Key Methods:**
- `startResearch(ResourceLocation)`: Begin researching a node
- `canStartResearch(ResourceLocation)`: Check if research can be started
- `completeResearch()`: Finish current research and apply rewards
- `tick()`: Called every game tick to advance research progress

### UI System

#### ResearchTableScreen
Multi-mode interface with three states:

1. **CLASS_SELECTION**: Shows four class buttons
2. **RESEARCH_TREE**: Shows available research nodes for selected class
3. **RESEARCH_DETAIL**: Shows details and cost for selected research node

```java
private enum UIMode {
    CLASS_SELECTION,
    RESEARCH_TREE,
    RESEARCH_DETAIL
}
```

**UI Flow:**
```
[Class Selection] → [Research Tree] → [Research Detail] → [Start Research]
      ↑                    ↑               ↑
   Back Button        Back Button     Back Button
```

## Adding New Research

### Method 1: Code-Based (Current Implementation)

New research nodes are added in `ResearchManager.initializeDefaultNodes()`:

```java
// Example: Adding a new Magician research node
private void createMagicianNodes() {
    // Ice Wand (Tier 1)
    ResourceLocation iceWandId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "ice_wand");
    List<ItemCost> iceWandCosts = List.of(
        new ItemCost(Items.PACKED_ICE, 4),
        new ItemCost(Items.STICK, 1),
        new ItemCost(Items.PAPER, 3)
    );
    ResearchNode iceWand = new ResearchNode(
        iceWandId,
        "Ice Wand",
        "Unlocks crafting recipe for Ice Wand that shoots ice bolts",
        ResearchClass.MAGICIAN,
        1, // Tier 1
        iceWandCosts,
        List.of(basicElementalId), // Requires Basic Elemental
        List.of(), // No cross-requisites
        new UnlockReward(UnlockReward.Type.RECIPE,
                        ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "ice_wand"))
    );

    allNodes.put(iceWandId, iceWand);
    nodesByClass.get(ResearchClass.MAGICIAN).add(iceWand);
}
```

### Method 2: JSON-Based (Future Enhancement)

The system is designed to support JSON-based research definitions:

```json
{
  "id": "hkbmod:ice_wand",
  "name": "Ice Wand",
  "description": "Unlocks crafting recipe for Ice Wand that shoots ice bolts",
  "class": "magician",
  "tier": 1,
  "cost": [
    {"item": "minecraft:packed_ice", "count": 4},
    {"item": "minecraft:stick", "count": 1},
    {"item": "minecraft:paper", "count": 3}
  ],
  "prerequisites": ["hkbmod:basic_elemental"],
  "cross_requisites": [],
  "unlock": {
    "type": "recipe",
    "recipe_id": "hkbmod:ice_wand"
  }
}
```

### Adding Cross-Class Dependencies

Example of a research that requires nodes from multiple classes:

```java
// Teleportation Orb - requires both Magician and Knight research
ResourceLocation teleportOrbId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "teleportation_orb");
ResearchNode teleportOrb = new ResearchNode(
    teleportOrbId,
    "Teleportation Orb",
    "Mystical orb combining magic and valor",
    ResearchClass.MAGICIAN,
    3, // High tier
    List.of(
        new ItemCost(Items.ENDER_PEARL, 4),
        new ItemCost(Items.DIAMOND, 2)
    ),
    List.of(fireWandId), // Requires Fire Wand from Magician tree
    List.of(basicDefenseId), // Requires Basic Defense from Knight tree
    new UnlockReward(UnlockReward.Type.ITEM, teleportOrbId)
);
```

## Research Progression Examples

### Current Default Trees

#### Magician Class
- **Tier 0**: Basic Elemental (5x Paper, 1x Stick)
- **Tier 1**: Fire Wand (3x Blaze Powder, 1x Stick, 5x Paper)
  - Prerequisite: Basic Elemental

#### Archer Class
- **Tier 0**: Basic Marksmanship (3x String, 2x Stick)

#### Knight Class
- **Tier 0**: Basic Defense (2x Iron Ingot, 3x Leather)

#### Cavalier Class
- **Tier 0**: Mounted Basics (1x Saddle, 4x Leather)

### Adding New Class

To add a new research class:

1. **Add to ResearchClass enum:**
```java
ARTIFICER("artificer", "Artificer", 0xFF9B59B6, "textures/gui/research/class_artificer.png")
```

2. **Create research nodes:**
```java
private void createArtificerNodes() {
    ResourceLocation basicCraftingId = ResourceLocation.fromNamespaceAndPath(HkbMod.MOD_ID, "basic_crafting");
    // ... implementation
}
```

3. **Call in initializeDefaultNodes():**
```java
public void initializeDefaultNodes() {
    if (!allNodes.isEmpty()) return;

    createMagicianNodes();
    createArcherNodes();
    createKnightNodes();
    createCavalierNodes();
    createArtificerNodes(); // Add new method
}
```

## Research Time Calculation

Research time is calculated based on tier:

```java
private int calculateResearchTime(ResearchNode node) {
    // Base time increases with tier: 10s per tier
    return (node.getTier() + 1) * 200; // 10 seconds in ticks per tier
}
```

**Examples:**
- Tier 0: 10 seconds (200 ticks)
- Tier 1: 20 seconds (400 ticks)
- Tier 2: 30 seconds (600 ticks)

## NBT Data Structure

Research data is saved in the following NBT structure:

```nbt
research: {
    unlocked: [
        {id: "hkbmod:basic_elemental"},
        {id: "hkbmod:fire_wand"}
    ],
    activeResearch: "hkbmod:ice_wand",
    progress: 150,
    maxProgress: 400,
    isResearching: true
}
```

## Integration with Other Systems

### Recipe Unlocking

When a research with `UnlockReward.Type.RECIPE` completes:

```java
private void applyUnlockReward(UnlockReward reward) {
    if (reward == null) return;

    switch (reward.getType()) {
        case RECIPE:
            // Add recipe to player's known recipes
            // Implementation depends on your recipe system
            break;
        case ITEM:
            // Give item directly to player
            break;
        case FLAG:
            // Set boolean flag for other mod systems
            break;
        case ABILITY:
            // Grant special ability
            break;
    }
}
```

### Custom Unlock Scripts

For complex unlock logic, use the script field:

```java
new UnlockReward(UnlockReward.Type.FLAG, targetId,
    "player.addPotionEffect(SPEED, 9999, 1)")
```

## Configuration

### Research Time Multiplier

To modify research times globally, adjust the calculation:

```java
private int calculateResearchTime(ResearchNode node) {
    float timeMultiplier = Config.RESEARCH_TIME_MULTIPLIER.get(); // From config
    return (int)((node.getTier() + 1) * 200 * timeMultiplier);
}
```

### Instant Research Mode

For development/testing:

```java
public void completeResearch() {
    if (Config.INSTANT_RESEARCH.get()) {
        // Skip time requirement
        researchProgress = researchMaxProgress;
    }

    if (researchProgress >= researchMaxProgress) {
        // Apply unlock rewards
    }
}
```

## Debugging and Logging

The system includes comprehensive logging:

```java
HkbMod.LOGGER.info("Research completed: {}", activeResearchId);
HkbMod.LOGGER.warn("Failed to load research id: {}", researchId);
HkbMod.LOGGER.info("Loaded {} research nodes", allNodes.size());
```

## Performance Considerations

- **Lazy Loading**: Research nodes are only initialized when first accessed
- **Efficient Filtering**: Available nodes are calculated on-demand
- **Minimal Ticking**: Only active research tables tick for progress
- **NBT Optimization**: Only essential data is saved to NBT

## Future Enhancements

### Planned Features
1. **JSON Configuration**: Load research trees from data packs
2. **Visual Research Tree**: Graphical node connections in UI
3. **Research Points**: Alternative currency system
4. **Team Research**: Collaborative research for multiplayer
5. **Research Discoveries**: Hidden nodes unlocked by experimentation

### Extension Points
- **Custom Unlock Types**: Extend UnlockReward.Type enum
- **Dynamic Prerequisites**: Conditional research requirements
- **Research Categories**: Sub-categories within classes
- **Progress Modifiers**: Items/blocks that speed up research

## Troubleshooting

### Common Issues

**Research not starting:**
- Check if prerequisites are met
- Verify required items are in research input slots (10-15)
- Ensure no other research is currently active

**Progress not advancing:**
- Confirm block entity is ticking (server-side only)
- Check if research table chunk is loaded
- Verify `tick()` method is being called

**NBT not saving:**
- Ensure `setChanged()` is called after state modifications
- Check NBT serialization methods for Optional API usage
- Verify block entity registration

**UI not updating:**
- Check client-server synchronization
- Verify screen mode transitions
- Ensure button bounds are correct

This documentation provides a complete guide to understanding, extending, and troubleshooting the research system. The modular design allows for easy expansion and customization while maintaining performance and compatibility.