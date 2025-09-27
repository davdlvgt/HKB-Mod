# Research System Implementation Plan

## Overview
This document outlines the implementation plan for a research system with 4 classes (Knight, Archer, Cavalier, Magician), a research table, and persistent progression saving.

## Core Components

### 1. Player Classes System
- **Knight**: Melee combat specialization
- **Archer**: Ranged combat specialization
- **Cavalier**: Mounted combat and mobility
- **Magician**: Magic and enchantment specialization

### 2. Research Table
- Custom block for conducting research
- GUI interface for browsing research trees
- Item consumption for research costs

### 3. Research Tree System
- Hierarchical unlock system
- Prerequisites and dependencies
- Visual tree representation

### 4. Persistence System
- Player data saving/loading
- World-specific or player-specific storage
- Sync between client and server

## Implementation Steps

### Phase 1: Foundation (Data Structures & Core Classes)

#### Step 1: Create Player Class Enum
```java
// File: src/main/java/de/davidvogt/hkbmod/research/PlayerClass.java
```
- Define the 4 player classes
- Include display names, colors, and icons
- Add utility methods for class identification

#### Step 2: Create Research Data Structures
```java
// File: src/main/java/de/davidvogt/hkbmod/research/Research.java
// File: src/main/java/de/davidvogt/hkbmod/research/ResearchTree.java
```
- Research object with ID, name, description, costs, prerequisites
- ResearchTree to manage research relationships
- ResearchType enum for different research categories

#### Step 3: Create Player Research Data
```java
// File: src/main/java/de/davidvogt/hkbmod/research/PlayerResearchData.java
```
- Track unlocked researches per player
- Track current player class
- Progress tracking for ongoing research

### Phase 2: Research Table Block & GUI

#### Step 4: Create Research Table Block
```java
// File: src/main/java/de/davidvogt/hkbmod/block/custom/ResearchTableBlock.java
// File: src/main/java/de/davidvogt/hkbmod/block/entity/ResearchTableBlockEntity.java
```
- Custom block with tile entity
- Inventory for research materials
- Right-click interaction to open GUI

#### Step 5: Research Table GUI
```java
// File: src/main/java/de/davidvogt/hkbmod/client/gui/ResearchTableScreen.java
// File: src/main/java/de/davidvogt/hkbmod/menu/ResearchTableMenu.java
```
- Container-based GUI system
- Research tree visualization
- Class selection interface
- Research progress display

#### Step 6: Register Research Table
- Add to ModBlocks registry
- Create block entity type
- Register menu type
- Add to creative tab

### Phase 3: Research Definitions & Trees

#### Step 7: Define Research Content
```java
// File: src/main/java/de/davidvogt/hkbmod/research/ModResearches.java
```
- Create research definitions for each class
- Define costs (items required)
- Set up prerequisite relationships
- Include research rewards (items/abilities)

#### Step 8: Research Tree Logic
```java
// File: src/main/java/de/davidvogt/hkbmod/research/ResearchManager.java
```
- Methods to check if research is available
- Research unlocking logic
- Cost validation and consumption
- Research tree traversal algorithms

### Phase 4: Persistence System

#### Step 9: Player Data Storage
```java
// File: src/main/java/de/davidvogt/hkbmod/research/PlayerResearchCapability.java
// File: src/main/java/de/davidvogt/hkbmod/research/PlayerResearchProvider.java
```
- Use Forge Capability system for player data
- Serialize/deserialize research progress
- Attach to player entities

#### Step 10: Data Synchronization
```java
// File: src/main/java/de/davidvogt/hkbmod/network/packets/SyncResearchDataPacket.java
```
- Network packets for client-server sync
- Update client when research is unlocked
- Handle class changes

### Phase 5: Integration & Research Rewards

#### Step 11: Research Rewards System
```java
// File: src/main/java/de/davidvogt/hkbmod/research/ResearchReward.java
// File: src/main/java/de/davidvogt/hkbmod/research/ResearchUnlockHandler.java
```
- Abstract reward system
- Item rewards, ability unlocks
- Recipe unlocking
- Stat modifications

#### Step 12: Class-Specific Items/Abilities
- Create class-specific items in ModItems
- Implement special abilities or enchantments
- Gate access behind research requirements

### Phase 6: UI/UX & Polish

#### Step 13: Research Tree Visualization
```java
// File: src/main/java/de/davidvogt/hkbmod/client/gui/components/ResearchTreeWidget.java
```
- Visual tree component with nodes and connections
- Interactive research nodes
- Progress indicators
- Zoom and pan functionality

#### Step 14: Class Selection UI
- Initial class selection screen
- Class change mechanics (if allowed)
- Visual class indicators

#### Step 15: Research Assets
- Create textures for research table
- Research node icons
- Class emblems and UI elements
- Localization files

### Phase 7: Data Generation & Configuration

#### Step 16: Data Generation
```java
// File: src/main/java/de/davidvogt/hkbmod/datagen/ResearchDataGenerator.java
```
- Auto-generate research table recipe
- Create research table model and blockstate
- Generate loot table

#### Step 17: Configuration System
```java
// File: src/main/java/de/davidvogt/hkbmod/research/ResearchConfig.java
```
- Configurable research costs
- Enable/disable specific research branches
- Class balance adjustments

### Phase 8: Testing & Balancing

#### Step 18: Integration Testing
- Test research unlocking
- Verify data persistence
- Check client-server synchronization
- Test class switching

#### Step 19: Balance Adjustments
- Adjust research costs
- Fine-tune progression pacing
- Balance class-specific rewards

## File Structure Summary

```
src/main/java/de/davidvogt/hkbmod/
├── research/
│   ├── PlayerClass.java
│   ├── Research.java
│   ├── ResearchTree.java
│   ├── PlayerResearchData.java
│   ├── ResearchManager.java
│   ├── ModResearches.java
│   ├── ResearchReward.java
│   ├── ResearchUnlockHandler.java
│   ├── PlayerResearchCapability.java
│   ├── PlayerResearchProvider.java
│   └── ResearchConfig.java
├── block/custom/
│   └── ResearchTableBlock.java
├── block/entity/
│   └── ResearchTableBlockEntity.java
├── client/gui/
│   ├── ResearchTableScreen.java
│   └── components/
│       └── ResearchTreeWidget.java
├── menu/
│   └── ResearchTableMenu.java
├── network/packets/
│   └── SyncResearchDataPacket.java
└── datagen/
    └── ResearchDataGenerator.java
```

## Technical Considerations

### Data Storage
- Use Forge Capabilities for player-attached data
- Consider using JSON files for research definitions
- Implement proper NBT serialization

### Network Communication
- Minimize packet size for research sync
- Use incremental updates where possible
- Handle edge cases (player disconnect during research)

### Performance
- Cache research tree calculations
- Lazy-load research data
- Optimize GUI rendering for large trees

### Extensibility
- Design system to allow easy addition of new researches
- Support for addon mods to add research content
- Modular reward system

## Integration Points

### Existing Mod Systems
- Integrate with existing item system
- Use established creative tabs
- Follow existing data generation patterns
- Maintain consistency with mod's architecture

### Minecraft Integration
- Proper advancement integration
- Recipe unlocking system
- Player statistics tracking
- Achievement system tie-ins

## Estimated Implementation Time
- **Phase 1-2**: 2-3 days (Foundation & Research Table)
- **Phase 3-4**: 2-3 days (Research Content & Persistence)
- **Phase 5-6**: 3-4 days (Rewards & UI Polish)
- **Phase 7-8**: 1-2 days (Generation & Testing)

**Total Estimated Time**: 8-12 days of development

## Next Steps
1. Review and approve this plan
2. Start with Phase 1 (Foundation)
3. Create basic data structures
4. Implement research table block
5. Build iteratively, testing each phase