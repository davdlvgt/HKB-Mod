package de.davidvogt.hkbmod.client.gui;

import de.davidvogt.hkbmod.research.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchOverviewScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("hkbmod", "textures/gui/research_overview.png");
    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 166;

    // Colors for different research states
    private static final int COLOR_AVAILABLE = 0xFF90EE90;    // Light Green
    private static final int COLOR_LOCKED = 0xFF696969;       // Dim Gray
    private static final int COLOR_COMPLETED = 0xFF32CD32;    // Lime Green
    private static final int COLOR_IN_PROGRESS = 0xFFFFD700;  // Gold
    private static final int COLOR_CONNECTION = 0xFF444444;   // Subtle gray

    // Layout constants for class columns
    private static final int NODE_WIDTH = 60;
    private static final int NODE_HEIGHT = 20;
    private static final int NODE_SPACING_X = 65; // Small gap between researches in same tier
    private static final int TIER_HEIGHT = 60; // Taller to accommodate multiple rows if needed
    private static final int TIER_LABEL_WIDTH = 60;
    private static final int LEFT_MARGIN = 80;
    private static final int CLASS_HEADER_HEIGHT = 25;
    private static final int MIN_COLUMN_WIDTH = 120; // Minimum width for columns
    private static final int COLUMN_PADDING = 20; // Padding between columns

    // Dynamic layout values
    private final Map<PlayerClass, Integer> columnWidths = new HashMap<>();
    private final Map<PlayerClass, Integer> columnPositions = new HashMap<>();

    private final ResearchTree researchTree;
    private final PlayerResearchData playerData;
    private int scrollX = 0; // Horizontal scrolling with limits
    private int scrollY = 0; // Vertical scrolling
    private boolean isDragging = false;
    private int lastMouseX = 0; // Track X position for dragging
    private int lastMouseY = 0; // Track Y position for dragging

    // Content boundaries
    private int contentMinX = 0;
    private int contentMaxX = 0;
    private int contentMinY = 0;
    private int contentMaxY = 0;
    private static final int SCROLL_MARGIN = 50; // Margin at edges

    // Research positioning cache
    private final Map<Research, Position> researchPositions = new HashMap<>();
    private final Map<PlayerClass, Position> classPositions = new HashMap<>();

    // Tooltip data
    private Research hoveredResearch = null;
    private PlayerClass hoveredClass = null;

    public ResearchOverviewScreen(ResearchTree researchTree, PlayerResearchData playerData) {
        super(Component.translatable("gui.hkbmod.research_overview.title"));
        this.researchTree = researchTree;
        this.playerData = playerData;


        calculatePositions();
    }

    @Override
    protected void init() {
        super.init();
        // Initialize with view at top of research tree
        scrollY = 0;
    }

    private void calculatePositions() {
        researchPositions.clear();
        classPositions.clear();
        columnWidths.clear();
        columnPositions.clear();

        // Define class column order
        PlayerClass[] columnOrder = {PlayerClass.KNIGHT, PlayerClass.ARCHER, PlayerClass.CAVALIER, PlayerClass.MAGICIAN};
        int startY = 70; // Leave space for class headers and tier labels

        // Organize researches by class and tier
        Map<PlayerClass, Map<Integer, List<Research>>> researchesByClassAndTier = new HashMap<>();
        int maxTier = 0;
        int minTier = 0;

        for (Research research : researchTree.getAllResearches()) {
            PlayerClass requiredClass = research.getRequiredClass();
            int tier = research.getTier();

            researchesByClassAndTier
                .computeIfAbsent(requiredClass, k -> new HashMap<>())
                .computeIfAbsent(tier, k -> new ArrayList<>())
                .add(research);

            maxTier = Math.max(maxTier, tier);
            minTier = Math.min(minTier, tier);
        }

        // Calculate dynamic column widths based on maximum researches per tier
        for (PlayerClass playerClass : columnOrder) {
            Map<Integer, List<Research>> tierResearches = researchesByClassAndTier.getOrDefault(playerClass, new HashMap<>());
            int maxResearchesInAnyTier = 1; // Minimum of 1

            for (List<Research> researches : tierResearches.values()) {
                maxResearchesInAnyTier = Math.max(maxResearchesInAnyTier, researches.size());
            }

            // Calculate required width: first research + (additional researches * spacing)
            int requiredWidth = NODE_WIDTH + (maxResearchesInAnyTier - 1) * NODE_SPACING_X;
            int columnWidth = Math.max(MIN_COLUMN_WIDTH, requiredWidth);
            columnWidths.put(playerClass, columnWidth);
        }

        // Calculate column positions based on dynamic widths
        int currentX = LEFT_MARGIN;
        for (PlayerClass playerClass : columnOrder) {
            columnPositions.put(playerClass, currentX);
            classPositions.put(playerClass, new Position(currentX, 40)); // Position for class headers
            currentX += columnWidths.get(playerClass) + COLUMN_PADDING;
        }

        // Position researches in their dynamic columns
        for (PlayerClass playerClass : columnOrder) {
            int columnX = columnPositions.get(playerClass);
            Map<Integer, List<Research>> tierResearches = researchesByClassAndTier.getOrDefault(playerClass, new HashMap<>());

            for (int tier = minTier; tier <= maxTier; tier++) {
                List<Research> researches = tierResearches.getOrDefault(tier, new ArrayList<>());
                int tierY = startY + tier * TIER_HEIGHT;

                // Sort researches by type priority: PASSIVE, COMBAT, UTILITY, CRAFTING, MAGIC
                researches.sort((r1, r2) -> Integer.compare(getTypePriority(r1.getType()), getTypePriority(r2.getType())));

                // Position multiple researches in the same tier/class horizontally
                for (int j = 0; j < researches.size(); j++) {
                    Research research = researches.get(j);
                    int x = columnX + j * NODE_SPACING_X; // Arrange horizontally
                    int y = tierY;
                    researchPositions.put(research, new Position(x, y));
                }
            }
        }

        // Calculate content boundaries
        calculateContentBounds();
    }

    private void calculateContentBounds() {
        if (researchPositions.isEmpty()) {
            contentMinX = 0;
            contentMaxX = 0;
            contentMinY = 0;
            contentMaxY = 0;
            return;
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        // Find bounds from all research positions
        for (Position pos : researchPositions.values()) {
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x + NODE_WIDTH);
            minY = Math.min(minY, pos.y);
            maxY = Math.max(maxY, pos.y + NODE_HEIGHT);
        }

        // Also include class headers with dynamic widths
        for (PlayerClass playerClass : classPositions.keySet()) {
            Position pos = classPositions.get(playerClass);
            Integer columnWidth = columnWidths.get(playerClass);
            if (pos != null && columnWidth != null) {
                minX = Math.min(minX, pos.x);
                maxX = Math.max(maxX, pos.x + columnWidth);
                minY = Math.min(minY, pos.y);
                maxY = Math.max(maxY, pos.y + CLASS_HEADER_HEIGHT);
            }
        }

        // Set content boundaries with margins
        contentMinX = minX - SCROLL_MARGIN;
        contentMaxX = maxX + SCROLL_MARGIN;
        contentMinY = minY - SCROLL_MARGIN;
        contentMaxY = maxY + SCROLL_MARGIN;
    }

    private void enforceScrollLimits() {
        // Calculate scroll bounds based on content and screen size
        // For horizontal: allow scrolling to show all content
        int maxScrollX = Math.max(0, contentMaxX - width + SCROLL_MARGIN);
        int minScrollX = Math.min(0, contentMinX - SCROLL_MARGIN);

        // For vertical: allow scrolling to show all content
        int maxScrollY = Math.max(0, contentMaxY - height + SCROLL_MARGIN);
        int minScrollY = Math.min(0, contentMinY - SCROLL_MARGIN);

        // Clamp scroll values (note: negative scrollX moves content right, positive moves left)
        scrollX = Math.max(-maxScrollX, Math.min(-minScrollX, scrollX));
        scrollY = Math.max(-maxScrollY, Math.min(-minScrollY, scrollY));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render simple background
        renderSimpleBackground(graphics);

        // Apply scroll offsets with limits
        enforceScrollLimits();
        int offsetX = scrollX;
        int offsetY = scrollY;

        // Render class headers
        renderClassHeaders(graphics, offsetX, offsetY);

        // Render tier labels on the left
        renderTierLabels(graphics, offsetY);

        // Render connections between researches
        renderConnections(graphics, offsetX, offsetY);

        // Render research nodes (fix hover detection)
        renderResearches(graphics, mouseX, mouseY, offsetX, offsetY);

        // Render tooltips (not affected by scroll)
        renderTooltips(graphics, mouseX, mouseY);

        // Render instructions
        renderInstructions(graphics);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderConnections(GuiGraphics graphics, int offsetX, int offsetY) {
        for (Research research : researchTree.getAllResearches()) {
            Position researchPos = researchPositions.get(research);
            if (researchPos == null) continue;

            for (ResourceLocation prerequisiteId : research.getPrerequisites()) {
                Research prerequisite = researchTree.getResearch(prerequisiteId);
                if (prerequisite == null) continue;

                Position prerequisitePos = researchPositions.get(prerequisite);
                if (prerequisitePos == null) continue;

                // Draw connection line
                drawConnection(graphics, prerequisitePos, researchPos, offsetX, offsetY);
            }
        }
    }

    private void drawConnection(GuiGraphics graphics, Position from, Position to, int offsetX, int offsetY) {
        int x1 = from.x + NODE_WIDTH / 2 + offsetX;
        int y1 = from.y + NODE_HEIGHT / 2 + offsetY;
        int x2 = to.x + NODE_WIDTH / 2 + offsetX;
        int y2 = to.y + NODE_HEIGHT / 2 + offsetY;

        // Draw a subtle connection line
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);

        if (steps > 0) {
            for (int i = 0; i <= steps; i++) {
                int x = x1 + (x2 - x1) * i / steps;
                int y = y1 + (y2 - y1) * i / steps;
                // Draw subtle line
                graphics.fill(x, y, x + 1, y + 1, COLOR_CONNECTION);
            }
        }
    }

    private void renderClassHeaders(GuiGraphics graphics, int offsetX, int offsetY) {
        PlayerClass[] columnOrder = {PlayerClass.KNIGHT, PlayerClass.ARCHER, PlayerClass.CAVALIER, PlayerClass.MAGICIAN};

        for (PlayerClass playerClass : columnOrder) {
            Integer columnX = columnPositions.get(playerClass);
            Integer columnWidth = columnWidths.get(playerClass);

            if (columnX == null || columnWidth == null) continue;

            int renderX = columnX + offsetX;
            int headerY = 40 + offsetY;

            // Only render if visible on screen
            if (headerY > -40 && headerY < height + 40 && renderX + columnWidth > 0 && renderX < width) {
                // Header background - dynamic width to span the column
                int headerWidth = columnWidth - 10; // Leave some margin
                int headerHeight = CLASS_HEADER_HEIGHT;

                // Simple background with class color
                int classColor = getClassColor(playerClass);
                graphics.fill(renderX, headerY, renderX + headerWidth, headerY + headerHeight, classColor);

                // Simple border
                graphics.fill(renderX - 1, headerY - 1, renderX + headerWidth + 1, headerY, 0xFF666666);
                graphics.fill(renderX - 1, headerY + headerHeight, renderX + headerWidth + 1, headerY + headerHeight + 1, 0xFF666666);
                graphics.fill(renderX - 1, headerY, renderX, headerY + headerHeight, 0xFF666666);
                graphics.fill(renderX + headerWidth, headerY, renderX + headerWidth + 1, headerY + headerHeight, 0xFF666666);

                // Class name text
                Component classText = Component.literal(playerClass.name());
                int textX = renderX + (headerWidth - font.width(classText)) / 2;
                int textY = headerY + 10;
                graphics.drawString(font, classText, textX, textY, 0xFFFFFFFF);
            }
        }
    }

    private void renderTierLabels(GuiGraphics graphics, int offsetY) {
        int startY = 70; // Match the research positioning

        // Find the tier range dynamically
        int maxTier = 0;
        int minTier = 0;
        for (Research research : researchTree.getAllResearches()) {
            maxTier = Math.max(maxTier, research.getTier());
            minTier = Math.min(minTier, research.getTier());
        }

        for (int tier = minTier; tier <= maxTier; tier++) {
            int tierY = startY + tier * TIER_HEIGHT + offsetY;

            // Only render if visible on screen
            if (tierY > -40 && tierY < height + 40) {
                // Tier label background - positioned to align with research rows
                int labelX = 10;
                int labelY = tierY + 5;
                int labelWidth = TIER_LABEL_WIDTH - 20;
                int labelHeight = 20;

                // Simple background
                graphics.fill(labelX, labelY, labelX + labelWidth, labelY + labelHeight, 0x88333333);

                // Simple border
                graphics.fill(labelX - 1, labelY - 1, labelX + labelWidth + 1, labelY, 0xFF555555);
                graphics.fill(labelX - 1, labelY + labelHeight, labelX + labelWidth + 1, labelY + labelHeight + 1, 0xFF555555);
                graphics.fill(labelX - 1, labelY, labelX, labelY + labelHeight, 0xFF555555);
                graphics.fill(labelX + labelWidth, labelY, labelX + labelWidth + 1, labelY + labelHeight, 0xFF555555);

                // Tier text
                Component tierText = Component.literal("Tier " + tier);
                int textX = labelX + (labelWidth - font.width(tierText)) / 2;
                int textY = labelY + 6;
                graphics.drawString(font, tierText, textX, textY, 0xFFCCCCCC);
            }
        }
    }

    private void renderResearches(GuiGraphics graphics, int mouseX, int mouseY, int offsetX, int offsetY) {
        // Reset hover state
        Research currentHover = null;

        for (Map.Entry<Research, Position> entry : researchPositions.entrySet()) {
            Research research = entry.getKey();
            Position pos = entry.getValue();

            // Check if mouse is hovering over this research (fix hover detection)
            int renderX = pos.x + offsetX;
            int renderY = pos.y + offsetY;
            boolean isHovered = mouseX >= renderX && mouseX < renderX + NODE_WIDTH &&
                              mouseY >= renderY && mouseY < renderY + NODE_HEIGHT;

            if (isHovered) {
                currentHover = research;
            }

            // Get simple colors
            int backgroundColor = getSimpleBackgroundColor(research);
            int borderColor = getSimpleBorderColor(research);
            int textColor = getSimpleTextColor(research);

            // Simple background
            graphics.fill(renderX, renderY, renderX + NODE_WIDTH, renderY + NODE_HEIGHT, backgroundColor);

            // Simple border
            if (isHovered) {
                // Thicker border when hovered
                graphics.fill(renderX - 2, renderY - 2, renderX + NODE_WIDTH + 2, renderY - 1, borderColor);
                graphics.fill(renderX - 2, renderY + NODE_HEIGHT + 1, renderX + NODE_WIDTH + 2, renderY + NODE_HEIGHT + 2, borderColor);
                graphics.fill(renderX - 2, renderY - 1, renderX - 1, renderY + NODE_HEIGHT + 1, borderColor);
                graphics.fill(renderX + NODE_WIDTH + 1, renderY - 1, renderX + NODE_WIDTH + 2, renderY + NODE_HEIGHT + 1, borderColor);
            } else {
                // Normal border
                graphics.fill(renderX - 1, renderY - 1, renderX + NODE_WIDTH + 1, renderY, borderColor);
                graphics.fill(renderX - 1, renderY + NODE_HEIGHT, renderX + NODE_WIDTH + 1, renderY + NODE_HEIGHT + 1, borderColor);
                graphics.fill(renderX - 1, renderY, renderX, renderY + NODE_HEIGHT, borderColor);
                graphics.fill(renderX + NODE_WIDTH, renderY, renderX + NODE_WIDTH + 1, renderY + NODE_HEIGHT, borderColor);
            }

            // Render research type icon
            renderResearchTypeIcon(graphics, research, renderX, renderY);

            // Render research name (slightly shifted to make room for icon)
            String truncatedName = truncateText(research.getName(), NODE_WIDTH - 20); // Leave more space for icon
            Component nameComponent = Component.literal(truncatedName);

            int nameX = renderX + 16; // Shift right to make room for icon
            int nameY = renderY + 8;
            graphics.drawString(font, nameComponent, nameX, nameY, textColor);
        }

        // Set the final hover state
        hoveredResearch = currentHover;
    }

    private void renderResearchTypeIcon(GuiGraphics graphics, Research research, int x, int y) {
        ResearchType type = research.getType();
        if (type == null) return;

        int iconSize = 12;
        int iconX = x + 2;
        int iconY = y + 2;

        // Background circle for icon
        int bgColor = getTypeBackgroundColor(type);
        graphics.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, bgColor);

        // Simple border
        int borderColor = getTypeBorderColor(type);
        graphics.fill(iconX - 1, iconY - 1, iconX + iconSize + 1, iconY, borderColor);
        graphics.fill(iconX - 1, iconY + iconSize, iconX + iconSize + 1, iconY + iconSize + 1, borderColor);
        graphics.fill(iconX - 1, iconY, iconX, iconY + iconSize, borderColor);
        graphics.fill(iconX + iconSize, iconY, iconX + iconSize + 1, iconY + iconSize, borderColor);

        // Render type symbol
        String symbol = getTypeSymbol(type);
        int symbolColor = getTypeSymbolColor(type);
        int textX = iconX + (iconSize - font.width(symbol)) / 2;
        int textY = iconY + 2;
        graphics.drawString(font, symbol, textX, textY, symbolColor);
    }

    private int getTypeBackgroundColor(ResearchType type) {
        return switch (type) {
            case COMBAT -> 0xFF4A1A1A;    // Dark red
            case CRAFTING -> 0xFF4A4A1A;  // Dark yellow
            case MAGIC -> 0xFF3A1A4A;     // Dark purple
            case UTILITY -> 0xFF1A1A4A;   // Dark blue
            case PASSIVE -> 0xFF1A4A1A;   // Dark green
        };
    }

    private int getTypeBorderColor(ResearchType type) {
        return switch (type) {
            case COMBAT -> 0xFFFF4444;    // Red
            case CRAFTING -> 0xFFFFAA44;  // Orange/Yellow
            case MAGIC -> 0xFFAA44FF;     // Purple
            case UTILITY -> 0xFF4444FF;   // Blue
            case PASSIVE -> 0xFF44FF44;   // Green
        };
    }

    private String getTypeSymbol(ResearchType type) {
        return switch (type) {
            case COMBAT -> "⚔";
            case CRAFTING -> "🔨";
            case MAGIC -> "✦";
            case UTILITY -> "🔧";
            case PASSIVE -> "🛡";
        };
    }

    private int getTypeSymbolColor(ResearchType type) {
        return switch (type) {
            case COMBAT -> 0xFFFFAAAA;    // Light red
            case CRAFTING -> 0xFFFFDDAA;  // Light orange
            case MAGIC -> 0xFFDDAAFF;     // Light purple
            case UTILITY -> 0xFFAAAAFF;   // Light blue
            case PASSIVE -> 0xFFAAFFAA;   // Light green
        };
    }

    private int getTypePriority(ResearchType type) {
        if (type == null) return 999; // Put null types at the end
        return switch (type) {
            case PASSIVE -> 1;
            case COMBAT -> 2;
            case UTILITY -> 3;
            case CRAFTING -> 4;
            case MAGIC -> 5;
        };
    }

    private int getSimpleBackgroundColor(Research research) {
        if (playerData.hasCompletedResearch(research.getId())) {
            return 0xFF2A4A2A; // Dark green
        } else if (playerData.isResearchInProgress(research.getId())) {
            return 0xFF4A4A2A; // Dark yellow
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            return 0xFF3A3A4A; // Dark blue-gray
        } else {
            return 0xFF3A3A3A; // Dark gray
        }
    }

    private int getSimpleBorderColor(Research research) {
        if (playerData.hasCompletedResearch(research.getId())) {
            return 0xFF4CAF50; // Green
        } else if (playerData.isResearchInProgress(research.getId())) {
            return 0xFFFFC107; // Amber
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            return 0xFF8BC34A; // Light green
        } else {
            return 0xFF666666; // Gray
        }
    }

    private int getSimpleTextColor(Research research) {
        if (playerData.hasCompletedResearch(research.getId())) {
            return 0xFFCCFFCC; // Light green
        } else if (playerData.isResearchInProgress(research.getId())) {
            return 0xFFFFFFCC; // Light yellow
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            return 0xFFCCCCCC; // Light gray
        } else {
            return 0xFF999999; // Gray
        }
    }

    private void drawModernBorder(GuiGraphics graphics, int x, int y, int width, int height, int color, boolean isHovered) {
        int borderWidth = isHovered ? 2 : 1;

        // Draw border with variable thickness
        for (int i = 0; i < borderWidth; i++) {
            // Top
            graphics.fill(x - i, y - i, x + width + i, y - i + 1, color);
            // Bottom
            graphics.fill(x - i, y + height + i - 1, x + width + i, y + height + i, color);
            // Left
            graphics.fill(x - i, y - i, x - i + 1, y + height + i, color);
            // Right
            graphics.fill(x + width + i - 1, y - i, x + width + i, y + height + i, color);
        }
    }

    private int[] getModernClassColors(PlayerClass playerClass, boolean isHovered) {
        // Returns [background, border, text]
        int baseColor = getClassColor(playerClass);
        boolean isUnlocked = playerData.hasUnlockedClass(playerClass);

        int background, border, text;

        if (isUnlocked) {
            background = isHovered ? mixColors(baseColor, 0xFFFFFFFF, 0.2f) : mixColors(baseColor, 0xFF000000, 0.3f);
            border = baseColor;
            text = 0xFFFFFFFF;
        } else {
            background = isHovered ? 0xFF3A3A3A : 0xFF2A2A2A;
            border = 0xFF616161;
            text = 0xFF9E9E9E;
        }

        return new int[]{background, border, text};
    }

    private void drawAdvancedShadow(GuiGraphics graphics, int x, int y, int width, int height) {
        // Multi-layer shadow for depth
        graphics.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x22000000);
        graphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x33000000);
        graphics.fill(x + 1, y + 1, x + width + 1, y + height + 1, 0x44000000);
    }

    private void drawRoundedGradientRect(GuiGraphics graphics, int x, int y, int width, int height, int baseColor, boolean isHovered) {
        // Main background
        graphics.fill(x, y, x + width, y + height, baseColor);

        // Gradient effect - darker at bottom, lighter at top
        int topColor = mixColors(baseColor, 0xFFFFFFFF, 0.15f);
        int bottomColor = mixColors(baseColor, 0xFF000000, 0.15f);

        // Create gradient by drawing horizontal lines with varying colors
        for (int i = 0; i < height / 2; i++) {
            float ratio = (float) i / (height / 2);
            int gradientColor = mixColors(topColor, baseColor, ratio);
            graphics.fill(x, y + i, x + width, y + i + 1, gradientColor);
        }

        for (int i = height / 2; i < height; i++) {
            float ratio = (float) (i - height / 2) / (height / 2);
            int gradientColor = mixColors(baseColor, bottomColor, ratio);
            graphics.fill(x, y + i, x + width, y + i + 1, gradientColor);
        }

        // Simulate rounded corners by drawing corner pixels
        drawRoundedCorners(graphics, x, y, width, height, baseColor);
    }

    private void drawRoundedCorners(GuiGraphics graphics, int x, int y, int width, int height, int baseColor) {
        // Soften corners with darker pixels (simple anti-aliasing effect)
        int cornerColor = mixColors(baseColor, 0xFF000000, 0.5f);

        // Top-left corner
        graphics.fill(x, y, x + 1, y + 1, cornerColor);
        // Top-right corner
        graphics.fill(x + width - 1, y, x + width, y + 1, cornerColor);
        // Bottom-left corner
        graphics.fill(x, y + height - 1, x + 1, y + height, cornerColor);
        // Bottom-right corner
        graphics.fill(x + width - 1, y + height - 1, x + width, y + height, cornerColor);
    }

    private void drawGlassHighlight(GuiGraphics graphics, int x, int y, int width, int height, int baseColor) {
        // Glass-like top highlight
        int highlightColor = mixColors(baseColor, 0xFFFFFFFF, 0.3f);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 3, highlightColor);

        // Subtle inner glow
        int innerGlow = mixColors(baseColor, 0xFFFFFFFF, 0.1f);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, innerGlow);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, innerGlow);
    }

    private void drawEnhancedBorder(GuiGraphics graphics, int x, int y, int width, int height, int color, boolean isHovered) {
        int borderWidth = isHovered ? 2 : 1;
        int brightColor = mixColors(color, 0xFFFFFFFF, 0.3f);

        for (int i = 0; i < borderWidth; i++) {
            int currentColor = i == 0 ? brightColor : color;
            // Top
            graphics.fill(x - i, y - i, x + width + i, y - i + 1, currentColor);
            // Bottom
            graphics.fill(x - i, y + height + i - 1, x + width + i, y + height + i, currentColor);
            // Left
            graphics.fill(x - i, y - i, x - i + 1, y + height + i, currentColor);
            // Right
            graphics.fill(x + width + i - 1, y - i, x + width + i, y + height + i, currentColor);
        }
    }

    private void drawHoverEffects(GuiGraphics graphics, int x, int y, int width, int height, int borderColor) {
        // Outer glow with multiple layers
        int glowColor1 = mixColors(borderColor, 0xFFFFFFFF, 0.5f) & 0x66FFFFFF;
        int glowColor2 = mixColors(borderColor, 0xFFFFFFFF, 0.3f) & 0x44FFFFFF;
        int glowColor3 = mixColors(borderColor, 0xFFFFFFFF, 0.1f) & 0x22FFFFFF;

        // Layer 1 - innermost glow
        graphics.fill(x - 1, y - 1, x + width + 1, y, glowColor1);
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, glowColor1);
        graphics.fill(x - 1, y, x, y + height, glowColor1);
        graphics.fill(x + width, y, x + width + 1, y + height, glowColor1);

        // Layer 2 - middle glow
        graphics.fill(x - 2, y - 2, x + width + 2, y - 1, glowColor2);
        graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, glowColor2);
        graphics.fill(x - 2, y - 1, x - 1, y + height + 1, glowColor2);
        graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, glowColor2);

        // Layer 3 - outer glow
        graphics.fill(x - 3, y - 3, x + width + 3, y - 2, glowColor3);
        graphics.fill(x - 3, y + height + 2, x + width + 3, y + height + 3, glowColor3);
        graphics.fill(x - 3, y - 2, x - 2, y + height + 2, glowColor3);
        graphics.fill(x + width + 2, y - 2, x + width + 3, y + height + 2, glowColor3);

        // Subtle inner pulse effect
        int pulseColor = mixColors(borderColor, 0xFFFFFFFF, 0.2f) & 0x33FFFFFF;
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, pulseColor);
    }

    private void drawTierBadge(GuiGraphics graphics, int x, int y, int tier, int borderColor) {
        String tierStr = String.valueOf(tier);
        int badgeSize = 14;
        int badgeX = x + NODE_WIDTH - badgeSize - 3;
        int badgeY = y + 3;

        // Badge shadow
        graphics.fill(badgeX + 1, badgeY + 1, badgeX + badgeSize + 1, badgeY + badgeSize + 1, 0x66000000);

        // Badge background with gradient
        int badgeBg = mixColors(borderColor, 0xFF000000, 0.4f);
        int badgeTop = mixColors(badgeBg, 0xFFFFFFFF, 0.2f);
        int badgeBottom = mixColors(badgeBg, 0xFF000000, 0.2f);

        // Draw gradient background
        for (int i = 0; i < badgeSize; i++) {
            float ratio = (float) i / badgeSize;
            int lineColor = mixColors(badgeTop, badgeBottom, ratio);
            graphics.fill(badgeX, badgeY + i, badgeX + badgeSize, badgeY + i + 1, lineColor);
        }

        // Badge border
        graphics.fill(badgeX - 1, badgeY - 1, badgeX + badgeSize + 1, badgeY, borderColor);
        graphics.fill(badgeX - 1, badgeY + badgeSize, badgeX + badgeSize + 1, badgeY + badgeSize + 1, borderColor);
        graphics.fill(badgeX - 1, badgeY, badgeX, badgeY + badgeSize, borderColor);
        graphics.fill(badgeX + badgeSize, badgeY, badgeX + badgeSize + 1, badgeY + badgeSize, borderColor);

        // Tier text with shadow
        int textWidth = font.width(tierStr);
        int textX = badgeX + (badgeSize - textWidth) / 2;
        int textY = badgeY + 3;

        // Text shadow
        graphics.drawString(font, tierStr, textX + 1, textY + 1, 0x88000000);
        // Text
        graphics.drawString(font, tierStr, textX, textY, 0xFFFFFFFF);
    }

    private int getResearchBackgroundColor(Research research) {
        if (playerData.hasCompletedResearch(research.getId())) {
            return 0xFF1a4a1a; // Dark green
        } else if (playerData.isResearchInProgress(research.getId())) {
            return 0xFF4a4a1a; // Dark yellow
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            return 0xFF2a3a2a; // Dark green-gray
        } else {
            return 0xFF2a2a2a; // Dark gray
        }
    }

    private int getResearchBorderColor(Research research) {
        if (playerData.hasCompletedResearch(research.getId())) {
            return 0xFF4CAF50; // Green
        } else if (playerData.isResearchInProgress(research.getId())) {
            return 0xFFFFC107; // Amber
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            return 0xFF8BC34A; // Light green
        } else {
            return 0xFF616161; // Gray
        }
    }

    private int getResearchTextColor(Research research) {
        if (playerData.hasCompletedResearch(research.getId())) {
            return 0xFFE8F5E8; // Light green
        } else if (playerData.isResearchInProgress(research.getId())) {
            return 0xFFFFF8E1; // Light yellow
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            return 0xFFE0E0E0; // Light gray
        } else {
            return 0xFF9E9E9E; // Gray
        }
    }

    private String truncateText(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String truncated = text;
        while (font.width(truncated + "...") > maxWidth && truncated.length() > 0) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated + "...";
    }

    private void renderTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (hoveredResearch != null) {
            renderResearchTooltip(graphics, hoveredResearch, mouseX, mouseY);
        } else if (hoveredClass != null) {
            renderClassTooltip(graphics, hoveredClass, mouseX, mouseY);
        }

        // Reset hover states
        hoveredResearch = null;
        hoveredClass = null;
    }

    private void renderResearchTooltip(GuiGraphics graphics, Research research, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(research.getDisplayName());
        tooltip.add(research.getDescriptionComponent());

        // Add status information
        if (playerData.hasCompletedResearch(research.getId())) {
            tooltip.add(Component.translatable("gui.hkbmod.research.status.completed").withStyle(net.minecraft.ChatFormatting.GREEN));
        } else if (playerData.isResearchInProgress(research.getId())) {
            tooltip.add(Component.translatable("gui.hkbmod.research.status.in_progress").withStyle(net.minecraft.ChatFormatting.YELLOW));
        } else if (researchTree.canUnlock(research, playerData.getCompletedResearches())) {
            tooltip.add(Component.translatable("gui.hkbmod.research.status.available").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        } else {
            tooltip.add(Component.translatable("gui.hkbmod.research.status.locked").withStyle(net.minecraft.ChatFormatting.RED));
        }

        // Add tier information
        tooltip.add(Component.translatable("gui.hkbmod.research.tier", research.getTier()).withStyle(net.minecraft.ChatFormatting.GRAY));

        // Add type information
        if (research.getType() != null) {
            tooltip.add(Component.literal("Type: ").withStyle(net.minecraft.ChatFormatting.GRAY)
                .append(research.getType().getDisplayComponent()));
        }

        renderSimpleTooltip(graphics, tooltip, mouseX, mouseY);
    }

    private void renderClassTooltip(GuiGraphics graphics, PlayerClass playerClass, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(playerClass.getDisplayComponent());
        tooltip.add(playerClass.getDescriptionComponent());

        // Add unlock status
        if (playerData.hasUnlockedClass(playerClass)) {
            tooltip.add(Component.translatable("gui.hkbmod.class.status.unlocked").withStyle(net.minecraft.ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("gui.hkbmod.class.status.locked").withStyle(net.minecraft.ChatFormatting.RED));
        }

        // Add research count
        int totalResearches = researchTree.getResearchCountForClass(playerClass);
        int completedResearches = (int) researchTree.getResearchesForClass(playerClass).stream()
            .filter(r -> playerData.hasCompletedResearch(r.getId()))
            .count();

        tooltip.add(Component.translatable("gui.hkbmod.class.progress", completedResearches, totalResearches)
            .withStyle(net.minecraft.ChatFormatting.GRAY));

        renderSimpleTooltip(graphics, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isDragging = true;
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            // Allow both horizontal and vertical scrolling
            scrollX += (int) (mouseX - lastMouseX);
            scrollY += (int) (mouseY - lastMouseY);
            lastMouseX = (int) mouseX;
            lastMouseY = (int) mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        // Support both vertical and horizontal scrolling
        if (Screen.hasShiftDown()) {
            // Horizontal scrolling with Shift + mouse wheel
            scrollX += (int) (deltaY * 20);
        } else {
            // Vertical scrolling with mouse wheel
            scrollY += (int) (deltaY * 20);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Don't pause the game
    }

    private void renderInstructions(GuiGraphics graphics) {
        List<Component> instructions = List.of(
            Component.literal("Research Overview").withStyle(net.minecraft.ChatFormatting.WHITE, net.minecraft.ChatFormatting.BOLD),
            Component.literal("Drag to pan • Shift+Scroll for horizontal • ESC to close").withStyle(net.minecraft.ChatFormatting.GRAY),
            Component.literal("Icons: 🛡Passive ⚔Combat 🔧Utility 🔨Crafting ✦Magic").withStyle(net.minecraft.ChatFormatting.GRAY),
            Component.literal("Green border: Completed • Light green: Available • Gray: Locked").withStyle(net.minecraft.ChatFormatting.GRAY)
        );

        int startY = 10;
        for (int i = 0; i < instructions.size(); i++) {
            Component instruction = instructions.get(i);
            int textY = startY + i * 12;

            // Background for readability
            int textWidth = font.width(instruction);
            graphics.fill(8, textY - 2, 12 + textWidth, textY + 10, 0x88000000);

            graphics.drawString(font, instruction, 10, textY, 0xFFFFFFFF);
        }
    }

    private void renderSimpleBackground(GuiGraphics graphics) {
        // Simple dark background
        graphics.fill(0, 0, width, height, 0xFF222222);
    }

    // Utility methods
    private void renderSimpleTooltip(GuiGraphics graphics, List<Component> tooltip, int mouseX, int mouseY) {
        if (tooltip.isEmpty()) return;

        // Calculate tooltip dimensions
        int maxWidth = 0;
        for (Component component : tooltip) {
            int width = font.width(component);
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        int tooltipWidth = maxWidth + 8;
        int tooltipHeight = tooltip.size() * 10 + 8;

        // Adjust position if tooltip would go off screen
        if (tooltipX + tooltipWidth > width) {
            tooltipX = mouseX - tooltipWidth - 12;
        }
        if (tooltipY + tooltipHeight > height) {
            tooltipY = mouseY - tooltipHeight + 12;
        }

        // Render background
        graphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, 0xCC000000);
        graphics.fill(tooltipX + 1, tooltipY + 1, tooltipX + tooltipWidth - 1, tooltipY + tooltipHeight - 1, 0xCC444444);

        // Render text
        for (int i = 0; i < tooltip.size(); i++) {
            Component component = tooltip.get(i);
            graphics.drawString(font, component, tooltipX + 4, tooltipY + 4 + i * 10, 0xFFFFFFFF);
        }
    }

    private int getClassColor(PlayerClass playerClass) {
        return switch (playerClass) {
            case KNIGHT -> 0xFFD0021B;      // Red
            case ARCHER -> 0xFF7ED321;      // Green
            case CAVALIER -> 0xFFB8860B;    // Gold
            case MAGICIAN -> 0xFF4A90E2;    // Blue
        };
    }

    private static int mixColors(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 * (1 - ratio) + r2 * ratio);
        int g = (int) (g1 * (1 - ratio) + g2 * ratio);
        int b = (int) (b1 * (1 - ratio) + b2 * ratio);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static class Position {
        final int x, y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}