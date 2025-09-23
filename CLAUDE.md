# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Minecraft mod called "HKB Mod" for Minecraft 1.21.7 using MinecraftForge 57.0.2. The mod adds custom items, blocks, and gameplay mechanics to Minecraft.

**Key Information:**
- **Mod ID:** `hkbmod`
- **Base Package:** `de.davidvogt.hkbmod`
- **Target Java Version:** 21
- **Minecraft Version:** 1.21.7
- **Forge Version:** 57.0.2

## Common Development Commands

### Building and Running
```bash
# Build the mod
./gradlew build

# Clean build directory
./gradlew clean

# Run client in development environment
./gradlew runClient

# Run dedicated server in development
./gradlew runServer

# Generate data (recipes, models, etc.)
./gradlew runData

# Run game test server
./gradlew runGameTestServer
```

### Development Tasks
```bash
# Generate IDE project files
./gradlew genIntellijRuns  # For IntelliJ IDEA
./gradlew genEclipseRuns   # For Eclipse
./gradlew genVSCodeRuns    # For VS Code

# Create JAR artifact
./gradlew jar

# Run tests
./gradlew test
```

## Architecture and Code Structure

### Core Architecture
The mod follows standard MinecraftForge patterns with these key components:

- **Main Mod Class:** `HkbMod.java` - Entry point with mod initialization
- **Registration System:** Uses deferred registers for items, blocks, entities, and creative tabs
- **Data Generation:** Automated generation of recipes, models, loot tables, and tags

### Package Organization
```
de.davidvogt.hkbmod/
├── HkbMod.java              # Main mod class
├── Config.java              # Mod configuration
├── block/                   # Custom blocks
│   ├── ModBlocks.java       # Block registry
│   └── custom/              # Custom block implementations
├── client/                  # Client-side code
│   ├── ClientTickHandler.java
│   ├── KeyInputHandler.java
│   └── renderer/            # Custom renderers
├── datagen/                 # Data generation classes
├── entities/                # Custom entities
├── item/                    # Custom items
│   ├── ModItems.java        # Item registry
│   ├── ModCreativeModeTabs.java
│   └── custom/              # Custom item implementations
└── util/                    # Utility classes
```

### Key Design Patterns

**Registration Pattern:**
All mod content (items, blocks, entities) uses MinecraftForge's `DeferredRegister` system for proper registration timing.

**Data Generation:**
The mod uses Forge's data generation system to automatically create:
- Item and block models
- Recipes
- Loot tables
- Tags
- Language files

**Entity System:**
Custom entities like `ThrowingKnifeEntity` are registered through `ModEntityTypes` and use custom renderers.

**Item Hierarchy:**
Complex items like throwing knives use abstract base classes (`AbstractThrowingKnifeItem`) for shared functionality.

## Mod Features

### Current Items
- **Alexandrite Tools:** Custom gem-based items
- **Mystical/Lightning Wands:** Magical items with special abilities
- **Throwing Knives:** Projectile weapons with variants (explosive, slowness, instant damage)
- **Feather Wings:** Mobility enhancement item
- **Growth Accelerator Wand:** Plant growth acceleration tool

### Special Systems
- **Key Sequence Management:** Custom input handling system
- **Client-Server Communication:** Separate client and server tick handlers
- **Custom Rendering:** Specialized renderers for entities like throwing knives

## Development Notes

### Gradle Configuration
- Uses ForgeGradle plugin for Minecraft mod development
- Requires 5GB RAM allocation (configured in gradle.properties)
- Supports automatic IDE run configuration generation
- Uses Parchment mappings for better parameter names

### Resource Generation
- Resources are automatically generated in `src/generated/resources/`
- Always run `runData` after adding new items/blocks to generate required assets
- Generated content includes models, recipes, and tags

### Testing
- Use `runClient` for in-game testing during development
- `runGameTestServer` for automated testing scenarios
- Build with `./gradlew build` before distributing

### File Locations
- Source code: `src/main/java/de/davidvogt/hkbmod/`
- Resources: `src/main/resources/`
- Generated resources: `src/generated/resources/`
- Assets: `src/main/resources/assets/hkbmod/`