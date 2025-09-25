# Research Table — System Design (Markdown)

> **Language:** English
>
> This document specifies a complete research system for a Minecraft mod. It describes the Research Table UI, class-based research trees (Magician, Archer, Knight, Cavalier), the interaction flow, data structures and implementation notes. Use this as a blueprint for implementation, asset design, and balancing.

---

## Introduction

The **Research Table** is a new workstation that allows players to research class-specific technologies. Each class has a branching research tree. Unlocking research nodes grants **recipes, items, abilities, or upgrades**. Some nodes require research from other classes (cross-requisites) and some nodes share common resource costs.

Goals:

* Provide meaningful progression for four classes: **Magician, Archer, Knight, Cavalier**.
* Keep the UI intuitive: left side is crafting, right side is class selection + research details.
* Allow selecting a node in a tree, returning to the main UI to deposit resources and complete the research.
* Support multiplayer cooperative research and table upgrades.

---

## High-Level UI Layout

```
-------------------------------- Research Table --------------------------------
| Left: Crafting Space (3x3 grid) | Right: Class Selector / Research Info   |
|                                 | [Magician] [Archer] [Knight] [Cavalier] |
|                                 | -> Click a class -> opens Research Tree |
--------------------------------------------------------------------------------
```

### Behavior

1. Player opens Research Table GUI.
2. Left: 3x3 *Crafting Space* identical to vanilla crafting. This lets the table also function as a crafting station.
3. Right: Shows four class icons. Clicking a class opens that class's research tree in a modal or full-right pane.
4. In the Research Tree view: nodes are shown as icons connected by lines. Hover shows tooltip (name, description, resource cost, prerequisites).
5. Clicking a research node **selects** it as the "active research" and **closes** the tree view, returning player to the main Research Table UI.
6. The right panel now shows the *resource requirements* to complete the selected research (counts & accepted items). Player deposits items into the table's input slots and starts research.
7. When requirements are satisfied, research completes either instantaneously or after a small progress time; reward is unlocked (recipe gets registered to player's known recipes).

---

## Research Node Design

Each research node is an object with the following core properties:

```json
{
  "id": "magician:fire_wand",
  "name": "Fire Wand",
  "description": "Unlocks crafting recipe for Fire Wand (Shoots fire bolts).",
  "class": "magician",
  "tier": 1,
  "cost": [
    {"item": "minecraft:blaze_powder", "count": 3},
    {"item": "minecraft:stick", "count": 1},
    {"item": "minecraft:paper", "count": 5}
  ],
  "prerequisites": ["magician:basic_elemental"],
  "cross_requisites": [],
  "unlock": {
    "type": "recipe",
    "recipe_id": "mod:fire_wand"
  }
}
```

Properties explained:

* `id`: unique namespaced id.
* `class`: which class tree this node sits in.
* `tier`: depth / level for balancing and UI layout.
* `cost`: list of item stacks required to perform research.
* `prerequisites`: nodes in the same tree that must be unlocked first.
* `cross_requisites`: nodes in other trees that must be unlocked first.
* `unlock`: the thing gained (recipe, ability token, passive modifier, item, or boolean flag). Include `script` entry for custom unlock behavior.

---

## Example Research Trees (high-level)

### Magician

* **Tier 0**: `basic_elemental` (intro)
* **Tier 1**: `fire_wand`, `ice_wand`, `mana_efficiency`
* **Tier 2**: `pyromancy_upgrade` (requires `fire_wand`), `frost_aura` (requires `ice_wand`), `arcane_staff` (requires `mana_efficiency`)
* **Tier 3**: `teleportation_orb` (cross_requisite: knight:valor)

### Archer

* **Tier 0**: `basic_marksmanship`
* **Tier 1**: `reinforced_bow`, `poison_arrows`, `light_boots`
* **Tier 2**: `sniper_bow` (requires `reinforced_bow`), `explosive_arrows` (requires `poison_arrows`), `double_jump` (requires `light_boots`)

### Knight

* **Tier 0**: `basic_defense`
* **Tier 1**: `iron_armor_upgrade`, `longsword`
* **Tier 2**: `heavy_plate` (requires `iron_armor_upgrade`), `runed_sword` (requires `longsword`)

### Cavalier

* **Tier 0**: `mounted_basics`
* **Tier 1**: `saddle_upgrade`, `lance`
* **Tier 2**: `warhorse_armor` (requires `saddle_upgrade`), `cavalry_charge` (requires `lance`)

---

## UI Flow: Selecting & Researching

1. **Class Selection**: Right panel shows class buttons. Hover shows summary and unlocked progress percentage.
2. **Open Research Tree**: Full-right pane shows node network. Nodes colored by state: locked, available, researched.
3. **Click Node**: Node becomes active. Modal closes and right panel displays the node's cost and `Start Research` button.
4. **Insert Resources**: Player drags items into designated research input slots below the crafting grid or into the table's internal inventory.
5. **Start Research**: If the inputs match the `cost`, the table consumes items and either:

    * Completes research instantly and unlocks recipe, or
    * Starts a progress timer (e.g., 10-60s depending on tier) with a visible progress bar and optional particle effects.
6. **Unlock**: Player receives the recipe added to their recipe book & the node is marked researched on the tree.

Notes: Add configurable settings for "instant research" vs "timed research" for modpack balancing.

---

## Inventory & Persistence

* The Research Table stores:

    * A 3x3 crafting grid, output slot, and result slot (behaves like crafting table)
    * A `research_input` inventory (N slots) for resource deposits
    * `active_research` slot storing the node id being researched
* Persist the table state in tile entity NBT: researched nodes, current progress, and stored inputs.

---

## Multiplayer

* **Shared Table State**: Table is world-bound; all players can see the same research progress.
* **Contribution Mode**: Allow multiple players to deposit resources into the `research_input`. The player who clicked `Start Research` gets the recipe unlock — or optionally share unlocks with nearby team members (configurable).
* **Permissions**: Respect player permissions/ownership if server runs claim protections.

---

## Implementation Hooks / Events

Expose events for modpack integration:

* `ResearchStarted(player, tablePos, nodeId)`
* `ResearchProgressTick(player, tablePos, nodeId, progress)`
* `ResearchCompleted(player, tablePos, nodeId)`

These allow other mods to react (spawn particles, trigger achievements, or require special items).

---

## Advanced Mechanics (Optional)

* **Table Upgrades**: Upgrade the Research Table with items (e.g., Redstone Core) to unlock higher-tier nodes or decrease research time.
* **Blueprints & Discovery**: Some nodes are hidden until prerequisites found or until player performs "discovery" craft in the crafting grid.
* **Random Research**: Chance to discover secret nodes during research completion.
* **Research Points**: Alternate economy where costs are paid as a combination of items and research points earned via activities.

---

## Visual & UX Notes

* Node icons should be clear and readable at small sizes. Use simple silhouettes.
* Use distinctive colors for each class to avoid confusion in overlapping nodes.
* Animate progress using floating runes or a circular progress bar.
* Provide audio cues on start and completion.

---

## Example: How to Add a New Node (Workflow for Modders)

1. Create a JSON file in `data/modid/research_nodes/` with the node object described above.
2. Add icons to `assets/modid/textures/gui/research_nodes/` named after the node id.
3. Register the unlockable recipe via normal recipe JSON and toggle its `hidden: true` until research unlocks it.
4. Listen to `ResearchCompleted` event to unlock special abilities or give players unique items.

---

## Balancing Tips

* Keep tier 0 and tier 1 nodes inexpensive so players can get early rewards.
* Scale costs and time with tier; tier 3-4 should require rarer materials and possibly cross-requisites.
* For multiplayer, consider scaling costs slightly higher or allowing collaborative discounts.

---

## Appendix: Example Node Set for Magician (Compact)

* `basic_elemental` — tutorial node, unlocks minor spells
* `fire_wand` — recipe: blaze_powder x3, stick x1, paper x5
* `ice_wand` — recipe: packed_ice x4, stick x1
* `mana_efficiency` — unlocks arcane staff recipe and reduces mana cost for spells
* `pyromancy_upgrade` — requires `fire_wand`, unlocks `flame_staff` recipe

---

## Final Notes

This design intentionally separates **selection** (choosing what to research) from **execution** (depositing resources and completing research) to create a clear, satisfying UX. It supports extensibility (new classes, nodes, cross-requisites) and multiplayer cooperation.

If you would like, I can also:

* Provide a JSON schema for the research node files.
* Produce example recipe JSON files for unlocked items.
* Produce mockup images of the UI.

---

*End of document.*
