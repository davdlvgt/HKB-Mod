# HKB Mod Items

This table contains all items included in the HKB Mod with their properties and descriptions.

| Item Name | Type | Stack Size | Durability | Special Properties | Description |
|-----------|------|------------|------------|-------------------|-------------|
| **Alexandrite** | Material | 64 | - | Basic material | A precious gemstone material |
| **Raw Alexandrite** | Raw Material | 64 | - | Raw ore material | Unprocessed alexandrite ore |
| **Chisel** | Tool | 64 | 32 | Wood transformation tool | Transforms wood blocks in sequence: Log → Wood → Planks → Stairs → Fence → Fence Gate → Log |
| **Kohlrabi** | Food | 64 | - | Food (3 nutrition, 0.25F saturation), 20% chance for Invisibility (20s) | Edible item that provides nutrition and sometimes grants invisibility |
| **Aurora Ashes** | Fuel | 64 | - | Fuel (1200 burn time) | A fuel item that burns for 60 seconds (1200 ticks) |
| **Mystical Wand** | Magic Tool | 1 | 64 | Shoots fireballs (2 damage) or snowballs (1 damage) when shift-clicking | Magical weapon that shoots projectiles, damages user |
| **Feather Wings** | Special Item | 64 | - | Unknown special properties | Item with custom functionality (details not visible in current code) |
| **Throwing Knife** | Projectile Weapon | 16 | - | Throwable projectile, can be imbued with potions | Basic throwing knife that can be enhanced with various potion effects |
| **Throwing Knife (Explosive)** | Projectile Weapon | 8 | - | Throwable projectile with explosive effect | Explosive variant of throwing knife with reduced stack size |
| **Throwing Knife (Slowness)** | Projectile Weapon | 12 | - | Throwable projectile with slowness effect | Throwing knife that applies slowness effect on hit |
| **Throwing Knife (Instant Damage)** | Projectile Weapon | 6 | - | Throwable projectile with instant damage effect | Throwing knife that deals instant damage, lowest stack size |
| **Lightning Axe** | Magic Tool/Axe | 1 | Variable | Axe functionality + Lightning strike ability (5s cooldown, 8 block radius, 20 strikes) | Enchantable axe that can summon lightning strikes around the player |
| **Lightning Wand** | Magic Tool | 1 | 100 | Lightning-based magic weapon | Magical wand with lightning-based abilities |
| **Growth Accelerator Wand** | Magic Tool | 1 | 128 | Key sequence challenge system for plant growth acceleration | Interactive magic wand that requires completing a 5-key sequence challenge to accelerate plant growth |

## Item Categories

### Materials & Resources
- Alexandrite (processed)
- Raw Alexandrite (raw ore)
- Aurora Ashes (fuel source)

### Food Items
- Kohlrabi (nutrition + invisibility chance)

### Tools & Weapons
- Chisel (wood transformation tool)
- Lightning Axe (magical axe with lightning abilities)

### Magic Items
- Mystical Wand (fireball/snowball shooter)
- Lightning Wand (lightning magic)
- Growth Accelerator Wand (plant growth acceleration)
- Feather Wings (special functionality)

### Projectile Weapons
- Throwing Knife (basic)
- Throwing Knife (Explosive)
- Throwing Knife (Slowness)
- Throwing Knife (Instant Damage)

## How to Use the Growth Accelerator Wand

The **Growth Accelerator Wand** is a unique magic tool that uses an interactive key sequence challenge system:

### Usage Steps:
1. **Target Selection**: Right-click on any growable plant or block
   - Supports: Crops (wheat, carrots, etc.), saplings, bamboo, sugar cane, cactus, chorus plants, nether wart, cocoa, sweet berry bushes
   - Can target farmland with crops above it
   - Can target the plant directly

2. **Key Sequence Challenge**: After right-clicking, a challenge UI appears above the hotbar showing:
   - **Title**: "Key Sequence Challenge"
   - **Key Sequence**: 5 random keys from the set {K, L, O, P, M}
   - **Progress Bar**: Shows time remaining (10 seconds total)
   - **Current Progress**: Displays completed vs total keys (e.g., "2/5")

3. **Visual Feedback**: Each key in the sequence has different colors:
   - **Green**: Correctly entered key
   - **Pulsing Yellow**: Current key to press
   - **Gray**: Future keys
   - **Red**: Incorrectly entered key (sequence resets)

4. **Key Input**: Press the displayed keys in the correct order using K, L, O, P, M keys
   - **Correct Key**: Plays a pleasant sound and advances progress
   - **Wrong Key**: Plays error sound and resets the sequence
   - **Timeout**: If 10 seconds pass, the challenge fails

5. **Success**: When all 5 keys are entered correctly:
   - Plays success sound
   - Shows "Sequence Complete! Growth Accelerated!" message
   - The targeted plant instantly grows to full maturity
   - Wand loses 1 durability point

### Key Features:
- **Interactive Challenge**: Requires skill and timing, not just clicking
- **Visual Feedback**: Clear UI shows progress and remaining time
- **Audio Cues**: Different sounds for success, failure, and progress
- **Fail-Safe**: Wrong keys reset the sequence rather than failing completely
- **Durability Cost**: Only consumes durability on successful growth acceleration

## Notes
- All throwing knives are based on the `AbstractThrowingKnifeItem` class
- The Lightning Axe functions as both a regular axe and a magical weapon
- Several items have custom functionality that affects gameplay mechanics
- Some items can be enhanced or imbued with additional effects
- Durability and damage values may vary based on enchantments and usage