# Research-basiertes Crafting System - Dokumentation

## Überblick

Dieses Dokument erklärt, wie Sie Items in Ihrem HKB Mod erst nach abgeschlossenen Forschungen craftbar machen können. Das System basiert auf einem Event-Handler-Ansatz, der das Crafting zur Laufzeit überwacht und kontrolliert.

## Architektur

Das Research-basierte Crafting System besteht aus folgenden Komponenten:

1. **Normale Rezepte** - Sichtbar im Crafting Interface
2. **ResearchCraftingHandler** - Event-Handler der Forschungsanforderungen prüft
3. **ResearchHelper** - Utility-Klasse für Forschungsüberprüfungen
4. **PlayerResearchDataManager** - Verwaltet persistente Forschungsdaten
5. **PlayerResearchEventHandler** - Automatisches Speichern/Laden

## Schritt-für-Schritt Anleitung

### 1. Normales Rezept hinzufügen

Fügen Sie Ihr Item-Rezept normal im `ModRecipeProvider` hinzu:

```java
// In ModRecipeProvider.java -> buildRecipes()
this.shaped(RecipeCategory.COMBAT, ModItems.YOUR_ITEM.get(), 1)
    .pattern("ABC")
    .pattern("DEF") 
    .pattern("GHI")
    .define('A', Items.MATERIAL_A)
    .define('B', Items.MATERIAL_B)
    // ... weitere Materialien
    .unlockedBy(getHasName(Items.MATERIAL_A), has(Items.MATERIAL_A))
    .save(this.output);
```

**Wichtig:** Fügen Sie einen Kommentar hinzu, der die Forschungsanforderung dokumentiert:

```java
// Your Item recipe - visible but only craftable with your_research_name research
this.shaped(RecipeCategory.COMBAT, ModItems.YOUR_ITEM.get(), 1)
    // ... Rezept Definition
```

### 2. Forschung definieren

Erstellen Sie eine JSON-Datei für Ihre Forschung in:
`src/main/resources/data/hkbmod/research/[class]/tier_[number]/[research_name].json`

```json
{
  "id": "hkbmod:your_research_name",
  "name": "Your Research Name",
  "description": "Description of what this research unlocks",
  "player_class": "ARCHER", // oder KNIGHT, MAGICIAN, CAVALIER
  "tier": 1,
  "type": "COMBAT", // oder MAGIC, UTILITY
  "prerequisites": [
    "hkbmod:prerequisite_research"
  ],
  "costs": [
    {
      "item": "minecraft:iron_ingot",
      "count": 5
    },
    {
      "item": "minecraft:stick",
      "count": 3
    }
  ]
}
```

### 3. ResearchHelper erweitern

Fügen Sie eine spezifische Methode für Ihr Item in `ResearchHelper.java` hinzu:

```java
/**
 * Check if a player can craft your item (requires your_research_name research)
 */
public static boolean canCraftYourItem(Player player) {
    ResourceLocation yourResearch = ResourceLocation.fromNamespaceAndPath("hkbmod", "your_research_name");
    return hasRequiredResearch(player, yourResearch);
}
```

### 4. ResearchCraftingHandler erweitern

Erweitern Sie den `ResearchCraftingHandler` um Ihr Item:

```java
@SubscribeEvent
public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
    ItemStack craftedItem = event.getCrafting();
    Player player = event.getEntity();

    // Existing throwing knife check
    if (craftedItem.getItem() == ModItems.THROWING_KNIFE.get()) {
        if (!ResearchHelper.canCraftThrowingKnife(player)) {
            preventCrafting(player, craftedItem, 
                List.of(
                    new ItemStack(Items.IRON_NUGGET, 1),
                    new ItemStack(Items.IRON_INGOT, 1),
                    new ItemStack(Items.STICK, 1)
                ),
                "You need to complete the 'Basic Archery' research to craft this item!"
            );
        }
    }
    
    // Add your new item check
    if (craftedItem.getItem() == ModItems.YOUR_ITEM.get()) {
        if (!ResearchHelper.canCraftYourItem(player)) {
            preventCrafting(player, craftedItem,
                List.of(
                    new ItemStack(Items.MATERIAL_A, 1),
                    new ItemStack(Items.MATERIAL_B, 1)
                    // ... weitere Materialien die zurückgegeben werden sollen
                ),
                "You need to complete the 'Your Research Name' research to craft this item!"
            );
        }
    }
}

private static void preventCrafting(Player player, ItemStack craftedItem, List<ItemStack> ingredients, String message) {
    // Remove the crafted item
    craftedItem.setCount(0);
    
    // Return ingredients to player
    if (!player.level().isClientSide) {
        for (ItemStack ingredient : ingredients) {
            player.addItem(ingredient);
        }
        
        // Send message to player
        player.displayClientMessage(
            net.minecraft.network.chat.Component.literal(message),
            true
        );
    }
}
```

## Beispiel: Komplette Implementierung

Hier ist ein vollständiges Beispiel für ein "Magic Sword" Item:

### 1. Rezept (ModRecipeProvider.java)
```java
// Magic sword recipe - visible but only craftable with advanced_magic research
this.shaped(RecipeCategory.COMBAT, ModItems.MAGIC_SWORD.get(), 1)
    .pattern(" M ")
    .pattern(" M ")
    .pattern(" S ")
    .define('M', ModItems.ALEXANDRITE.get())
    .define('S', Items.STICK)
    .unlockedBy(getHasName(ModItems.ALEXANDRITE.get()), has(ModItems.ALEXANDRITE.get()))
    .save(this.output);
```

### 2. Forschung (advanced_magic.json)
```json
{
  "id": "hkbmod:advanced_magic",
  "name": "Advanced Magic",
  "description": "Unlock the ability to craft magical weapons",
  "player_class": "MAGICIAN",
  "tier": 2,
  "type": "MAGIC",
  "prerequisites": [
    "hkbmod:basic_magic"
  ],
  "costs": [
    {
      "item": "hkbmod:alexandrite",
      "count": 10
    },
    {
      "item": "minecraft:book",
      "count": 5
    }
  ]
}
```

### 3. ResearchHelper Erweiterung
```java
/**
 * Check if a player can craft the magic sword (requires advanced_magic research)
 */
public static boolean canCraftMagicSword(Player player) {
    ResourceLocation advancedMagic = ResourceLocation.fromNamespaceAndPath("hkbmod", "advanced_magic");
    return hasRequiredResearch(player, advancedMagic);
}
```

### 4. ResearchCraftingHandler Erweiterung
```java
// In onItemCrafted method
if (craftedItem.getItem() == ModItems.MAGIC_SWORD.get()) {
    if (!ResearchHelper.canCraftMagicSword(player)) {
        preventCrafting(player, craftedItem,
            List.of(
                new ItemStack(ModItems.ALEXANDRITE.get(), 2),
                new ItemStack(Items.STICK, 1)
            ),
            "You need to complete the 'Advanced Magic' research to craft this item!"
        );
    }
}
```

## Automatisches Speichern/Laden

Das System verwendet bereits automatisches Speichern und Laden durch den `PlayerResearchEventHandler`. Forschungsdaten werden:

- **Beim Login geladen** - Aus persistenten NBT-Daten
- **Beim Logout gespeichert** - In persistente NBT-Daten
- **Bei Respawn übertragen** - Forschungen bleiben nach dem Tod erhalten
- **Automatisch gespeichert** - Wenn neue Forschungen abgeschlossen werden

## Best Practices

### 1. Konsistente Namenskonvention
- Forschungs-IDs: `mod_id:research_name` (lowercase, underscores)
- Helper-Methoden: `canCraft[ItemName](Player player)`
- JSON-Dateien: `research_name.json`

### 2. Logische Forschungsstruktur
- Organisieren Sie Forschungen in sinnvolle Tiers
- Verwenden Sie Prerequisites für Forschungsketten
- Gruppieren Sie ähnliche Items unter gemeinsame Forschungen

### 3. Benutzerfreundliche Nachrichten
- Klare, verständliche Fehlermeldungen
- Nennen Sie den exakten Namen der benötigten Forschung
- Verwenden Sie einheitliche Nachrichtenformate

### 4. Materialien-Management
- Geben Sie immer alle verwendeten Materialien zurück
- Listen Sie Materialien in der gleichen Reihenfolge wie im Rezept
- Berücksichtigen Sie Stapelgrößen korrekt

## Debugging und Testen

### 1. Log-Ausgaben
Das System gibt automatisch Log-Ausgaben aus:
```
[ResearchEventHandler] Player logged in: PlayerName - Research data loaded
[PlayerResearchDataManager] Loaded research data for player: PlayerName - Unlocked researches: 3
```

### 2. Testen ohne Forschung
1. Neuen Spieler erstellen
2. Versuchen Sie das Item zu craften
3. Überprüfen Sie, ob Materialien zurückgegeben werden
4. Überprüfen Sie die angezeigte Nachricht

### 3. Testen mit Forschung
1. Forschung über Research Table abschließen
2. Spiel verlassen und neu starten
3. Überprüfen Sie, ob Forschung noch vorhanden ist
4. Testen Sie das Crafting - sollte jetzt funktionieren

## Erweiterte Funktionen

### 1. Mehrere Forschungen für ein Item
```java
public static boolean canCraftAdvancedItem(Player player) {
    ResourceLocation research1 = ResourceLocation.fromNamespaceAndPath("hkbmod", "research_1");
    ResourceLocation research2 = ResourceLocation.fromNamespaceAndPath("hkbmod", "research_2");
    
    return hasRequiredResearch(player, research1) && 
           hasRequiredResearch(player, research2);
}
```

### 2. Klassenspezifische Items
```java
public static boolean canCraftArcherItem(Player player) {
    // Überprüfe sowohl Forschung als auch Spielerklasse
    PlayerResearchData data = PlayerResearchDataManager.getPlayerResearchData(player);
    return data.getPlayerClass() == PlayerClass.ARCHER && 
           hasRequiredResearch(player, ResourceLocation.fromNamespaceAndPath("hkbmod", "archer_research"));
}
```

### 3. Progressive Freischaltung
```java
public static boolean canCraftTierItem(Player player, int requiredTier) {
    PlayerResearchData data = PlayerResearchDataManager.getPlayerResearchData(player);
    int completedTierResearches = data.getUnlockedResearchCountForClass(
        data.getPlayerClass(), 
        ResearchManager.getGlobalResearchTree()
    );
    return completedTierResearches >= requiredTier;
}
```

## Dateien die Sie bearbeiten müssen

Für jedes neue forschungsbasierte Item müssen Sie folgende Dateien bearbeiten:

1. ✅ **ModRecipeProvider.java** - Normales Rezept hinzufügen
2. ✅ **ResearchHelper.java** - Neue canCraft-Methode hinzufügen  
3. ✅ **ResearchCraftingHandler.java** - Event-Handler erweitern
4. ✅ **[research_name].json** - Neue Forschungsdefinition erstellen

Das wars! Das System kümmert sich automatisch um Speichern, Laden und Persistierung.

## Fehlerbehebung

### Problem: Forschung wird nicht gespeichert
- Überprüfen Sie, ob `PlayerResearchEventHandler` registriert ist
- Prüfen Sie die Log-Ausgaben beim Login/Logout
- Stellen Sie sicher, dass `unlockResearch()` mit Player-Parameter aufgerufen wird

### Problem: Rezept funktioniert nicht
- Überprüfen Sie die ResearchHelper-Methode
- Prüfen Sie die Forschungs-ID Schreibweise
- Testen Sie mit Debug-Ausgaben

### Problem: Materialien werden nicht zurückgegeben
- Überprüfen Sie die preventCrafting-Materialien-Liste
- Stellen Sie sicher, dass alle verwendeten Materialien aufgelistet sind
- Prüfen Sie Stapelgrößen

Mit diesem System können Sie beliebig viele Items mit Forschungsanforderungen erstellen!
