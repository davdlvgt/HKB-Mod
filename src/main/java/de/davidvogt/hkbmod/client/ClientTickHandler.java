package de.davidvogt.hkbmod.client;

import de.davidvogt.hkbmod.util.KeySequenceManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        // Update the key sequence manager each tick
        KeySequenceManager.getInstance().tick();

        // Show key sequence progress in action bar
        showKeySequenceProgress();
    }

    private static void showKeySequenceProgress() {
        KeySequenceManager manager = KeySequenceManager.getInstance();

        if (!manager.isSequenceActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        List<Character> sequence = manager.getCurrentSequence();
        List<Character> userInput = manager.getUserInput();
        int ticksRemaining = manager.getTicksRemaining();

        if (sequence.isEmpty()) return;

        // Create action bar message showing the sequence and progress
        StringBuilder message = new StringBuilder("§6Sequence: ");
        for (int i = 0; i < sequence.size(); i++) {
            if (i > 0) message.append(" ");

            char key = sequence.get(i);
            if (i < userInput.size()) {
                // Already entered - show in green
                message.append("§a").append(Character.toUpperCase(key));
            } else if (i == userInput.size()) {
                // Current key to press - show in yellow with highlighting
                message.append("§e§l[").append(Character.toUpperCase(key)).append("]");
            } else {
                // Future key - show in gray
                message.append("§7").append(Character.toUpperCase(key));
            }
        }

        // Add progress and time remaining
        message.append(" §f(").append(userInput.size()).append("/").append(sequence.size()).append(") ");
        message.append("§c").append(ticksRemaining / 20).append("s");

        // Display as action bar message
        mc.player.displayClientMessage(Component.literal(message.toString()), true);
    }

    private static void renderKeySequenceOverlay() {
        KeySequenceManager manager = KeySequenceManager.getInstance();

        if (!manager.isSequenceActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        List<Character> sequence = manager.getCurrentSequence();
        List<Character> userInput = manager.getUserInput();
        int ticksRemaining = manager.getTicksRemaining();

        if (sequence.isEmpty()) return;

        // Get GuiGraphics from the current GUI context
        GuiRenderState renderState = new GuiRenderState();
        GuiGraphics guiGraphics = new GuiGraphics(mc, renderState);
        Font font = mc.font;

        // Calculate position above hotbar
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int centerX = screenWidth / 2;
        int sequenceY = screenHeight - 80; // Above hotbar

        // Draw background
        int bgWidth = sequence.size() * 25 + 20;
        int bgHeight = 40;
        int bgX = centerX - bgWidth / 2;
        int bgY = sequenceY - 5;

        // Semi-transparent black background
        guiGraphics.fill(bgX, bgY, bgX + bgWidth, bgY + bgHeight, 0x80000000);

        // Draw border
        guiGraphics.fill(bgX - 1, bgY - 1, bgX + bgWidth + 1, bgY, 0xFFFFFFFF); // Top
        guiGraphics.fill(bgX - 1, bgY + bgHeight, bgX + bgWidth + 1, bgY + bgHeight + 1, 0xFFFFFFFF); // Bottom
        guiGraphics.fill(bgX - 1, bgY, bgX, bgY + bgHeight, 0xFFFFFFFF); // Left
        guiGraphics.fill(bgX + bgWidth, bgY, bgX + bgWidth + 1, bgY + bgHeight, 0xFFFFFFFF); // Right

        // Draw title
        String title = "Key Sequence Challenge";
        int titleWidth = font.width(title);
        guiGraphics.drawString(font, title, centerX - titleWidth / 2, sequenceY - 35, 0xFFFFFF);

        // Draw the complete sequence as text for clarity
        StringBuilder sequenceText = new StringBuilder("Sequence: ");
        for (int i = 0; i < sequence.size(); i++) {
            if (i > 0) sequenceText.append(" → ");
            sequenceText.append(Character.toUpperCase(sequence.get(i)));
        }
        String sequenceStr = sequenceText.toString();
        int sequenceWidth = font.width(sequenceStr);
        guiGraphics.drawString(font, sequenceStr, centerX - sequenceWidth / 2, sequenceY - 20, 0xFFAAFFFF);

        // Draw timeout bar
        float timeoutProgress = (float) ticksRemaining / 200.0f; // 200 is the total timeout
        int barWidth = bgWidth - 10;
        int barX = bgX + 5;
        int barY = bgY + bgHeight + 5;

        // Timeout bar background
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 4, 0x80000000);

        // Timeout bar fill (red when low, green when high)
        int barFillWidth = (int) (barWidth * timeoutProgress);
        int barColor = timeoutProgress > 0.3f ? 0xFF00FF00 : 0xFFFF0000; // Green or red
        if (barFillWidth > 0) {
            guiGraphics.fill(barX, barY, barX + barFillWidth, barY + 4, barColor);
        }

        // Draw sequence keys
        int startX = centerX - (sequence.size() * 25) / 2;

        for (int i = 0; i < sequence.size(); i++) {
            char key = sequence.get(i);
            int keyX = startX + i * 25;
            int keyY = sequenceY + 5;

            // Determine key state and color
            int keyColor;
            int bgColor;
            if (i < userInput.size()) {
                char inputKey = userInput.get(i);
                if (inputKey == key) {
                    // Correct key entered
                    keyColor = 0xFF00FF00; // Green
                    bgColor = 0xFF004400; // Dark green background
                } else {
                    // Wrong key entered
                    keyColor = 0xFFFF0000; // Red
                    bgColor = 0xFF440000; // Dark red background
                }
            } else if (i == userInput.size()) {
                // Current key to press
                // Pulsing effect
                float pulse = (float) Math.sin((System.currentTimeMillis() % 1000) / 1000.0 * Math.PI * 2) * 0.3f + 0.7f;
                int pulseBrightness = (int) (255 * pulse);
                keyColor = (0xFF << 24) | (pulseBrightness << 16) | (pulseBrightness << 8) | 0x00; // Pulsing yellow
                bgColor = 0xFF444400; // Dark yellow background
            } else {
                // Future key
                keyColor = 0xFFAAAAAA; // Gray
                bgColor = 0xFF222222; // Dark gray background
            }

            // Draw key background
            guiGraphics.fill(keyX - 8, keyY - 8, keyX + 8, keyY + 8, bgColor);

            // Draw key border
            guiGraphics.fill(keyX - 9, keyY - 9, keyX + 9, keyY - 8, 0xFFFFFFFF); // Top
            guiGraphics.fill(keyX - 9, keyY + 8, keyX + 9, keyY + 9, 0xFFFFFFFF); // Bottom
            guiGraphics.fill(keyX - 9, keyY - 8, keyX - 8, keyY + 8, 0xFFFFFFFF); // Left
            guiGraphics.fill(keyX + 8, keyY - 8, keyX + 9, keyY + 8, 0xFFFFFFFF); // Right

            // Draw key letter
            String keyStr = String.valueOf(Character.toUpperCase(key));
            int keyWidth = font.width(keyStr);
            guiGraphics.drawString(font, keyStr, keyX - keyWidth / 2, keyY - 4, keyColor);

            // Draw order number below the key
            String orderStr = String.valueOf(i + 1);
            int orderWidth = font.width(orderStr);
            guiGraphics.drawString(font, orderStr, keyX - orderWidth / 2, keyY + 10, 0xFFFFFFFF);
        }

        // Draw progress indicator
        String progress = userInput.size() + "/" + sequence.size();
        int progressWidth = font.width(progress);
        guiGraphics.drawString(font, progress, centerX - progressWidth / 2, sequenceY + 25, 0xFFFFFF);

        // Draw current key hint
        if (userInput.size() < sequence.size()) {
            char nextKey = sequence.get(userInput.size());
            String hint = "Press: " + Character.toUpperCase(nextKey);
            int hintWidth = font.width(hint);

            // Pulsing effect for the hint text
            float pulse = (float) Math.sin((System.currentTimeMillis() % 800) / 800.0 * Math.PI * 2) * 0.4f + 0.6f;
            int pulseBrightness = (int) (255 * pulse);
            int hintColor = (0xFF << 24) | (pulseBrightness << 16) | (pulseBrightness << 8) | 0x00; // Pulsing yellow

            guiGraphics.drawString(font, hint, centerX - hintWidth / 2, sequenceY + 40, hintColor);
        }

        // Rendering is automatically handled by GuiGraphics
    }
}