package de.davidvogt.hkbmod.research;

import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class ResearchTree {
    private final Map<ResourceLocation, Research> researches;
    private final Map<PlayerClass, List<Research>> researchesByClass;
    private final Map<ResourceLocation, Set<ResourceLocation>> dependents;

    public ResearchTree() {
        this.researches = new HashMap<>();
        this.researchesByClass = new EnumMap<>(PlayerClass.class);
        this.dependents = new HashMap<>();

        // Initialize class lists
        for (PlayerClass playerClass : PlayerClass.values()) {
            researchesByClass.put(playerClass, new ArrayList<>());
        }
    }

    public void addResearch(Research research) {
        if (researches.containsKey(research.getId())) {
            throw new IllegalArgumentException("Research with ID " + research.getId() + " already exists");
        }

        researches.put(research.getId(), research);
        researchesByClass.get(research.getRequiredClass()).add(research);

        // Build dependency graph
        for (ResourceLocation prerequisite : research.getPrerequisites()) {
            dependents.computeIfAbsent(prerequisite, k -> new HashSet<>()).add(research.getId());
        }
    }

    public Research getResearch(ResourceLocation id) {
        return researches.get(id);
    }

    public List<Research> getResearchesForClass(PlayerClass playerClass) {
        return new ArrayList<>(researchesByClass.get(playerClass));
    }

    public List<Research> getResearchesByTier(PlayerClass playerClass, int tier) {
        return researchesByClass.get(playerClass).stream()
                .filter(research -> research.getTier() == tier)
                .collect(Collectors.toList());
    }

    public List<Research> getAvailableResearches(PlayerClass playerClass, Set<ResourceLocation> unlockedResearches) {
        return researchesByClass.get(playerClass).stream()
                .filter(research -> !unlockedResearches.contains(research.getId()))
                .filter(research -> arePrerequisitesMet(research, unlockedResearches))
                .collect(Collectors.toList());
    }

    public boolean arePrerequisitesMet(Research research, Set<ResourceLocation> unlockedResearches) {
        return unlockedResearches.containsAll(research.getPrerequisites());
    }

    public boolean canUnlock(Research research, Set<ResourceLocation> unlockedResearches) {
        return !unlockedResearches.contains(research.getId())
               && arePrerequisitesMet(research, unlockedResearches);
    }

    public Set<ResourceLocation> getDependents(ResourceLocation researchId) {
        return new HashSet<>(dependents.getOrDefault(researchId, Collections.emptySet()));
    }

    public List<Research> getDirectPrerequisites(Research research) {
        return research.getPrerequisites().stream()
                .map(this::getResearch)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Research> getDirectDependents(Research research) {
        return getDependents(research.getId()).stream()
                .map(this::getResearch)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int getMaxTierForClass(PlayerClass playerClass) {
        return researchesByClass.get(playerClass).stream()
                .mapToInt(Research::getTier)
                .max()
                .orElse(0);
    }

    public List<Research> getRootResearches(PlayerClass playerClass) {
        return researchesByClass.get(playerClass).stream()
                .filter(research -> research.getPrerequisites().isEmpty())
                .collect(Collectors.toList());
    }

    public List<Research> getClassUnlockResearches() {
        return researches.values().stream()
                .filter(Research::isClassUnlock)
                .collect(Collectors.toList());
    }

    public boolean hasResearch(ResourceLocation id) {
        return researches.containsKey(id);
    }

    public Collection<Research> getAllResearches() {
        return Collections.unmodifiableCollection(researches.values());
    }

    public int getTotalResearchCount() {
        return researches.size();
    }

    public int getResearchCountForClass(PlayerClass playerClass) {
        return researchesByClass.get(playerClass).size();
    }

    public List<Research> getResearchPath(Research target, Set<ResourceLocation> unlockedResearches) {
        List<Research> path = new ArrayList<>();
        Set<ResourceLocation> visited = new HashSet<>();

        if (findPath(target, unlockedResearches, visited, path)) {
            Collections.reverse(path);
            return path;
        }

        return Collections.emptyList();
    }

    private boolean findPath(Research current, Set<ResourceLocation> unlockedResearches,
                           Set<ResourceLocation> visited, List<Research> path) {
        if (unlockedResearches.contains(current.getId())) {
            return true;
        }

        if (visited.contains(current.getId())) {
            return false;
        }

        visited.add(current.getId());

        for (ResourceLocation prerequisiteId : current.getPrerequisites()) {
            Research prerequisite = getResearch(prerequisiteId);
            if (prerequisite != null && findPath(prerequisite, unlockedResearches, visited, path)) {
                path.add(current);
                return true;
            }
        }

        return current.getPrerequisites().isEmpty();
    }

    public void clear() {
        researches.clear();
        researchesByClass.values().forEach(List::clear);
        dependents.clear();
    }
}