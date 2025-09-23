# HKB Mod Coding Conventions

This document outlines the coding standards and conventions for the HKB Minecraft mod project.

## Table of Contents
- [General Guidelines](#general-guidelines)
- [Java Code Style](#java-code-style)
- [Package Organization](#package-organization)
- [Naming Conventions](#naming-conventions)
- [Registration Patterns](#registration-patterns)
- [Data Generation](#data-generation)
- [File Organization](#file-organization)
- [Documentation](#documentation)
- [Git & GitHub Workflow](#git--github-workflow)

## General Guidelines

- **Target Java Version**: Java 21
- **Minecraft Version**: 1.21.7
- **Forge Version**: 57.0.2
- **Base Package**: `de.davidvogt.hkbmod`
- **Mod ID**: `hkbmod`

## Java Code Style

### Code Formatting
- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Always use curly braces for if/else statements
- Place opening braces on the same line

### Import Organization
```java
// Standard Java imports first
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Minecraft imports
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

// Forge imports
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

// Mod imports last
import de.davidvogt.hkbmod.HkbMod;
import de.davidvogt.hkbmod.item.ModItems;
```

### Class Structure
1. Static constants
2. Static fields
3. Instance fields
4. Constructors
5. Static methods
6. Instance methods
7. Inner classes

## Package Organization

```
de.davidvogt.hkbmod/
├── HkbMod.java                    # Main mod class
├── Config.java                    # Configuration
├── block/                         # Block-related classes
│   ├── ModBlocks.java            # Block registry
│   └── custom/                   # Custom block implementations
├── client/                       # Client-side code only
│   ├── ClientTickHandler.java
│   ├── KeyInputHandler.java
│   └── renderer/                 # Custom renderers
├── datagen/                      # Data generation classes
│   ├── DataGenerators.java      # Main data generator registry
│   ├── ModModelProvider.java    # Model generation
│   ├── ModLanguageProvider.java # Language file generation
│   └── Mod*Provider.java        # Other data providers
├── entities/                     # Custom entities
│   ├── ModEntityTypes.java      # Entity registry
│   └── *.java                   # Entity implementations
├── item/                         # Item-related classes
│   ├── ModItems.java            # Item registry
│   ├── ModCreativeModeTabs.java # Creative tabs
│   ├── ModFoodProperties.java   # Food properties
│   └── custom/                  # Custom item implementations
└── util/                        # Utility classes
```

## Naming Conventions

### Classes
- **Registry Classes**: `Mod{Type}s` (e.g., `ModItems`, `ModBlocks`, `ModEntityTypes`)
- **Custom Implementations**: `{Name}{Type}Item` (e.g., `GrowthAcceleratorWandItem`, `ThrowingKnifeItem`)
- **Data Generators**: `Mod{Type}Provider` (e.g., `ModModelProvider`, `ModLanguageProvider`)
- **Abstract Classes**: `Abstract{Name}` (e.g., `AbstractThrowingKnifeItem`)

### Fields and Methods
```java
// Registry objects - UPPER_SNAKE_CASE
public static final RegistryObject<Item> GROWTH_ACCELERATOR_WAND = ITEMS.register(...);

// Regular fields - camelCase
private final ItemStack itemStack;

// Methods - camelCase
public void accelerateGrowth() { ... }

// Constants - UPPER_SNAKE_CASE
public static final String MOD_ID = "hkbmod";
```

### Resource Names
- **Items/Blocks**: `snake_case` (e.g., `growth_accelerator_wand`, `throwing_knife_explosive`)
- **Textures**: Match item/block names exactly
- **Models**: Match item/block names exactly

## Registration Patterns

### DeferredRegister Pattern
```java
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, HkbMod.MOD_ID);

    public static final RegistryObject<Item> ITEM_NAME = ITEMS.register("item_name",
            () -> new CustomItem(new Item.Properties()
                    .stacksTo(16)
                    .setId(ResourceKey.create(Registries.ITEM,
                            ResourceLocation.parse("hkbmod:item_name")))));

    public static void register(BusGroup busGroup) {
        ITEMS.register(busGroup);
    }
}
```

### Resource Key Pattern
Always set explicit resource keys for items and blocks:
```java
.setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse("hkbmod:item_name")))
```

## Data Generation

### File Structure
- **Combined Providers**: Use `ModModelProvider` for both item models and block models/states
- **Separate Concerns**: Keep recipes, loot tables, tags, and languages in separate providers
- **Automation First**: Always prefer data generation over manual JSON files

### Provider Naming
```java
// Good
public class ModModelProvider extends ModelProvider { ... }
public class ModLanguageProvider extends LanguageProvider { ... }
public class ModRecipeProvider extends RecipeProvider { ... }

// Avoid
public class ItemModelProvider { ... } // Too specific
public class MyDataProvider { ... }    // Not descriptive
```

### Data Generation Registration
```java
@Mod.EventBusSubscriber(modid = HkbMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Server-side data
        generator.addProvider(event.includeServer(), new ModRecipeProvider.Generator(packOutput, lookupProvider));

        // Client-side data
        generator.addProvider(event.includeClient(), new ModModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "en_us"));
    }
}
```

## File Organization

### Resource Structure
```
src/main/resources/
├── META-INF/
│   └── mods.toml
├── assets/hkbmod/
│   └── textures/
│       ├── item/
│       └── block/
└── data/hkbmod/
    └── (manual data files only)

src/generated/resources/          # Auto-generated files
├── assets/hkbmod/
│   ├── blockstates/
│   ├── items/
│   ├── lang/
│   └── models/
└── data/hkbmod/
    ├── loot_table/
    ├── recipe/
    └── tags/
```

### Build Commands
```bash
# Generate all data files
./gradlew runData

# Build the mod
./gradlew build

# Run client for testing
./gradlew runClient

# Run server for testing
./gradlew runServer
```

## Documentation

### Class Documentation
```java
/**
 * Registry class for all custom items in the HKB mod.
 *
 * <p>Items are registered using the DeferredRegister pattern and
 * automatically assigned resource keys for proper identification.</p>
 */
public class ModItems {
    // ...
}
```

### Method Documentation
```java
/**
 * Accelerates the growth of the target block if it's a crop or sapling.
 *
 * @param level the level where the block is located
 * @param pos the position of the block to accelerate
 * @param player the player using the wand
 * @return true if growth was accelerated, false otherwise
 */
public boolean accelerateGrowth(Level level, BlockPos pos, Player player) {
    // ...
}
```

### Comments
- Use `//` for single-line comments
- Use `/* */` for multi-line comments within methods
- Use `/** */` for JavaDoc documentation
- Avoid obvious comments
- Focus on explaining "why" not "what"

### Language Conventions
- **English Only**: All code, comments, and documentation in English
- **German Comments**: Only in special cases where working with German-specific content
- **Consistent Terminology**: Use Minecraft/Forge terminology consistently

## Best Practices

### Error Handling
```java
// Good - Defensive programming
if (level.isClientSide()) {
    return InteractionResult.PASS;
}

// Good - Null checks
if (player == null || itemStack.isEmpty()) {
    return false;
}
```

### Performance
- Cache expensive operations
- Use `level.isClientSide()` checks appropriately
- Prefer `CompletableFuture` for async operations in data generation

### Security
- Never expose sensitive information in logs
- Validate all input parameters
- Use proper access modifiers (`private`, `protected`, `public`)

### Testing
- Always test with `./gradlew runClient` before committing
- Test both single-player and multiplayer scenarios
- Verify data generation with `./gradlew runData`

## Git & GitHub Workflow

### Branch Management

#### Branch Naming Conventions
Use descriptive branch names following these patterns:

```
# Feature branches
feature/item-growth-accelerator-wand
feature/throwing-knife-variants
feature/client-side-key-handling

# Bug fixes
fix/recipe-generation-crash
fix/item-model-missing-texture
fix/multiplayer-sync-issue

# Improvements/refactoring
improve/data-generation-performance
refactor/item-registration-system
update/minecraft-1.21.8

# Documentation
docs/coding-conventions
docs/setup-guide

# Hotfixes (critical production issues)
hotfix/critical-dupe-bug
hotfix/server-crash-on-startup

# Experimental/research
experiment/new-entity-system
research/performance-optimization
```

#### Branch Structure
- **`master`** - **PROTECTED** - Production-ready code, only playable stable releases. Only receives merges from `release`
- **`release`** - Testing branch for release candidates. Receives features from `dev` when ready for testing
- **`dev`** - Active development branch. All new features merge here first
- **`feature/*`** - New features and enhancements (branched from `dev`)
- **`fix/*`** - Bug fixes (branched from `dev` or `release` depending on severity)
- **`hotfix/*`** - Critical fixes that bypass normal flow (branched from `master`)

### Commit Message Conventions

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### Commit Types
- **`feat:`** - New feature
- **`fix:`** - Bug fix
- **`docs:`** - Documentation changes
- **`style:`** - Code style changes (formatting, etc.)
- **`refactor:`** - Code refactoring
- **`test:`** - Adding or updating tests
- **`chore:`** - Maintenance tasks
- **`build:`** - Build system changes
- **`ci:`** - CI/CD changes

#### Examples
```bash
# Good commit messages
feat(items): add growth accelerator wand with plant growth mechanics
fix(datagen): resolve duplicate model provider registration issue
docs: add coding conventions for git workflow
refactor(items): extract abstract throwing knife base class
style(datagen): fix import organization in ModModelProvider
chore: update Forge version to 57.0.2

# Poor commit messages (avoid these)
git commit -m "fix stuff"
git commit -m "update"
git commit -m "changes"
git commit -m "wip"
```

### Workflow Process

#### Branch Flow Overview
```
master (protected) ← release ← dev ← feature/fix branches
     ↑                  ↑        ↑
   Stable            Testing   Development
```

#### 1. Creating a New Feature
```bash
# Start from dev branch
git checkout dev
git pull origin dev

# Create feature branch
git checkout -b feature/lightning-wand-item

# Work on your feature
# Make commits following conventions
git add .
git commit -m "feat(items): add lightning wand with strike ability"

# Push branch
git push -u origin feature/lightning-wand-item
```

#### 2. Before Creating Pull Request to Dev
```bash
# Ensure data generation works
./gradlew runData

# Build and test
./gradlew build
./gradlew runClient  # Test in-game

# Verify no build errors
./gradlew clean build

# Update your branch with latest dev
git checkout dev
git pull origin dev
git checkout feature/lightning-wand-item
git rebase dev  # Keep clean history
```

#### 3. Feature → Dev Pull Request
- **Target Branch**: `dev`
- **Requirements**: Basic functionality testing
- **Review Level**: Standard code review
- **Auto-merge**: Can be enabled for trusted contributors

#### 4. Dev → Release Process
```bash
# When ready to prepare a release
git checkout release
git pull origin release

# Merge dev into release
git merge dev
git push origin release

# Create release preparation PR
# Target: release branch
# Purpose: Final integration testing
```

#### 5. Release → Master Process
```bash
# After thorough testing on release branch
git checkout master
git pull origin master

# Create PR from release to master
# This should be a formal release PR
# Requires extensive testing and approval
```

#### 6. Hotfix Process (Emergency Fixes)
```bash
# For critical bugs in production (master)
git checkout master
git pull origin master

# Create hotfix branch
git checkout -b hotfix/critical-dupe-bug

# Fix the issue
git add .
git commit -m "hotfix: resolve item duplication exploit"

# Push and create PR to master
git push -u origin hotfix/critical-dupe-bug

# After master merge, also merge into release and dev
git checkout release
git merge master
git checkout dev
git merge release
```

### Pull Request Guidelines by Branch

#### Feature/Fix → Dev PRs
##### PR Title Format
```
<type>: <description>

# Examples
feat: Add lightning wand with electrical strike mechanics
fix: Resolve crash when using growth accelerator on invalid blocks
```

##### PR Checklist (Dev)
- [ ] Branch is up-to-date with dev
- [ ] All commits follow naming conventions
- [ ] Data generation runs without errors (`./gradlew runData`)
- [ ] Mod builds successfully (`./gradlew build`)
- [ ] Basic functionality tested (`./gradlew runClient`)
- [ ] No console errors or warnings
- [ ] Added translations for new content

#### Dev → Release PRs
##### PR Title Format
```
release: Prepare version X.Y.Z for testing

# Example
release: Prepare version 1.3.0 for testing with lightning wand and throwing knife improvements
```

##### PR Checklist (Release)
- [ ] All features in dev are complete
- [ ] Version bumped in `gradle.properties`
- [ ] Changelog updated
- [ ] Full integration testing completed
- [ ] Multiplayer compatibility verified
- [ ] Performance impact assessed
- [ ] No breaking changes without migration path

#### Release → Master PRs (Protected)
##### PR Title Format
```
chore: Release version X.Y.Z

# Example
chore: Release version 1.3.0 - Lightning wand and throwing knife improvements
```

##### PR Checklist (Master - STRICT)
- [ ] **MANDATORY**: Extensive testing completed on release branch
- [ ] **MANDATORY**: Version number finalized
- [ ] **MANDATORY**: Changelog is complete and accurate
- [ ] **MANDATORY**: No known critical bugs
- [ ] **MANDATORY**: Performance regression testing passed
- [ ] **MANDATORY**: Multiplayer stability confirmed
- [ ] **MANDATORY**: Compatible with current Minecraft/Forge versions
- [ ] Release notes prepared for GitHub release

### Branch Protection Rules

#### Master Branch Protection
- **Require pull request reviews**: 2+ reviewers
- **Require status checks**: All CI/CD must pass
- **Require branches to be up to date**: Yes
- **Restrict pushes**: Only release branch can merge
- **Require signed commits**: Recommended
- **Include administrators**: Yes (no exceptions)

#### Release Branch Protection
- **Require pull request reviews**: 1+ reviewer
- **Require status checks**: Build and basic tests must pass
- **Allow force pushes**: No
- **Restrict pushes**: Only dev branch and hotfixes can merge

#### Dev Branch Protection
- **Require pull request reviews**: 1 reviewer (can be less strict)
- **Require status checks**: Basic build must pass
- **Allow force pushes**: With lease (for rebasing)
- **Delete head branches**: Automatic cleanup enabled

### Testing Requirements by Branch

#### Dev Branch Testing
- ✅ Code compiles without errors
- ✅ Data generation works (`./gradlew runData`)
- ✅ Basic functionality works in single-player
- ⚠️ Multiplayer testing recommended but not required
- ⚠️ Performance impact noted but not blocking

#### Release Branch Testing
- ✅ All dev branch requirements
- ✅ **MANDATORY**: Full multiplayer testing
- ✅ **MANDATORY**: Performance regression testing
- ✅ **MANDATORY**: Integration testing with existing features
- ✅ **MANDATORY**: No critical bugs or crashes
- ✅ Compatibility testing with popular other mods (recommended)

#### Master Branch Testing
- ✅ All release branch requirements
- ✅ **MANDATORY**: Extended playtesting (minimum 24-48 hours)
- ✅ **MANDATORY**: Community testing (beta testers)
- ✅ **MANDATORY**: Documentation is complete
- ✅ **MANDATORY**: Rollback plan exists if issues arise

### Repository Maintenance

#### .gitignore Essentials
```gitignore
# Build outputs
build/
run/
run-data/
logs/

# IDE files
.idea/
*.iml
*.ipr
*.iws
.vscode/

# Gradle
.gradle/

# OS files
.DS_Store
Thumbs.db

# Development
*.log
*.tmp
.cache/

# Don't ignore textures and manual resources
!src/main/resources/assets/*/textures/
!src/main/resources/assets/*/sounds/
!src/main/resources/META-INF/

# Ignore generated resources (these should be regenerated)
src/generated/resources/
```

#### Release Management
```bash
# Creating releases
git checkout master
git pull origin master

# Update version in gradle.properties
# mod_version=1.2.0

git add gradle.properties
git commit -m "chore: bump version to 1.2.0"

# Create and push tag
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin v1.2.0
git push origin master

# Create GitHub release from tag with changelog
```

#### Issue Management

##### Issue Labels
- `bug` - Something isn't working
- `enhancement` - New feature or request
- `documentation` - Improvements to documentation
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `priority:high` - High priority
- `priority:low` - Low priority
- `status:in-progress` - Currently being worked on
- `status:blocked` - Blocked by other issues/dependencies

##### Issue Templates
Create `.github/ISSUE_TEMPLATE/` with:

**Bug Report Template:**
```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. See error

**Expected behavior**
What you expected to happen.

**Environment:**
- Minecraft Version: [e.g. 1.21.7]
- Forge Version: [e.g. 57.0.2]
- Mod Version: [e.g. 1.0.0]

**Additional context**
Add any other context about the problem here.
```

**Feature Request Template:**
```markdown
**Feature Description**
A clear description of what you want to happen.

**Use Case**
Explain the use case or problem this feature would solve.

**Proposed Implementation**
If you have ideas about how to implement this, describe them here.

**Additional context**
Add any other context or screenshots about the feature request.
```

### Security Considerations

#### Sensitive Information
- Never commit API keys, passwords, or tokens
- Use environment variables or config files (ignored by git)
- Review commits before pushing to ensure no sensitive data

#### Dependencies
- Regularly update dependencies for security patches
- Use `./gradlew dependencies` to check for vulnerabilities
- Document any security-related changes in commit messages

---

*This document should be updated as the project evolves and new patterns emerge.*