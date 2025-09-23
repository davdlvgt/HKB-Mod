package de.davidvogt.hkbmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.*;

public class KeySequenceManager {
    private static KeySequenceManager instance;

    private final List<Character> validKeys = Arrays.asList('g', 'h', 'j', 'u');
    private List<Character> currentSequence = new ArrayList<>();
    private List<Character> userInput = new ArrayList<>();
    private boolean sequenceActive = false;
    private BlockPos targetPos = null;
    private int ticksRemaining = 0;
    private static final int SEQUENCE_TIMEOUT = 200; // 10 seconds (20 ticks = 1 second)

    public static KeySequenceManager getInstance() {
        if (instance == null) {
            instance = new KeySequenceManager();
        }
        return instance;
    }

    public void startSequence(BlockPos pos) {
        this.targetPos = pos;
        this.sequenceActive = true;
        this.userInput.clear();
        this.ticksRemaining = SEQUENCE_TIMEOUT;

        // Generate random sequence of 5 keys
        this.currentSequence.clear();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            this.currentSequence.add(validKeys.get(random.nextInt(validKeys.size())));
        }

        // Play start sound
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            mc.player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1.0F, 1.0F);
            mc.player.displayClientMessage(Component.literal("§aKey Sequence Challenge Started!"), true);

            // Debug message to show the sequence
            StringBuilder seqDebug = new StringBuilder("§6Sequence: ");
            for (int i = 0; i < currentSequence.size(); i++) {
                if (i > 0) seqDebug.append(" → ");
                seqDebug.append(Character.toUpperCase(currentSequence.get(i)));
            }
            mc.player.displayClientMessage(Component.literal(seqDebug.toString()), false);
        }
    }

    public boolean handleKeyInput(char key) {
        if (!sequenceActive || currentSequence.isEmpty()) {
            return false;
        }

        // Convert to lowercase for consistency
        key = Character.toLowerCase(key);

        // Debug logging
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§7Key pressed: " + Character.toUpperCase(key)), false);
        }

        // Check if it's a valid key
        if (!validKeys.contains(key)) {
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§cInvalid key: " + Character.toUpperCase(key)), false);
            }
            return false;
        }

        userInput.add(key);

        // Check if this key matches the expected key
        int expectedIndex = userInput.size() - 1;
        if (expectedIndex < currentSequence.size()) {
            char expectedKey = currentSequence.get(expectedIndex);

            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§7Expected: " + Character.toUpperCase(expectedKey) + ", Got: " + Character.toUpperCase(key)), false);
            }

            if (key == expectedKey) {
                // Correct key
                if (mc.level != null && mc.player != null) {
                    mc.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0F, 1.2F);
                }

                // Check if sequence is complete
                if (userInput.size() == currentSequence.size()) {
                    if (mc.player != null) {
                        mc.player.displayClientMessage(Component.literal("§aSequence completed successfully!"), false);
                    }
                    completeSequence(true);
                } else {
                    if (mc.player != null) {
                        mc.player.displayClientMessage(Component.literal("§7Progress: " + userInput.size() + "/" + currentSequence.size()), false);
                    }
                }
            } else {
                // Wrong key - reset sequence
                if (mc.level != null && mc.player != null) {
                    mc.player.playSound(SoundEvents.NOTE_BLOCK_BASS.value(), 1.0F, 0.8F);
                    mc.player.displayClientMessage(Component.literal("§cWrong key! Sequence reset."), true);
                }
                userInput.clear();
            }
        }

        return true;
    }

    public void tick() {
        if (!sequenceActive) {
            return;
        }

        ticksRemaining--;
        if (ticksRemaining <= 0) {
            completeSequence(false);
        }
    }

    private void completeSequence(boolean success) {
        sequenceActive = false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            mc.player.displayClientMessage(Component.literal("§eCompleteSequence called with success=" + success), false);

            if (success) {
                mc.player.playSound(SoundEvents.PLAYER_LEVELUP, 1.0F, 1.0F);
                mc.player.displayClientMessage(Component.literal("§aSequence Complete! Growth Accelerated!"), true);

                // Mark the sequence as completed on the player's held item and trigger server-side growth
                if (mc.player != null) {
                    var heldItem = mc.player.getMainHandItem();
                    if (heldItem.getItem() instanceof de.davidvogt.hkbmod.item.custom.GrowthAcceleratorWandItem) {
                        var tag = heldItem.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();

                        String posStr = targetPos.getX() + "," + targetPos.getY() + "," + targetPos.getZ();
                        tag.putString("SequenceCompleted", posStr);

                        heldItem.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                            net.minecraft.world.item.component.CustomData.of(tag));

                        // Just mark completion - the wand will handle growth on next use
                        mc.player.displayClientMessage(Component.literal("§aNow right-click the plant again to grow it!"), true);
                    }
                }
            } else {
                mc.player.playSound(SoundEvents.ITEM_BREAK.value(), 1.0F, 1.0F);
                mc.player.displayClientMessage(Component.literal("§cSequence Timeout! Try again."), true);
            }
        }

        // Reset state
        currentSequence.clear();
        userInput.clear();
        targetPos = null;
        ticksRemaining = 0;
    }

    public boolean isSequenceActive() {
        return sequenceActive;
    }

    public List<Character> getCurrentSequence() {
        return new ArrayList<>(currentSequence);
    }

    public List<Character> getUserInput() {
        return new ArrayList<>(userInput);
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public void cancelSequence() {
        if (sequenceActive) {
            sequenceActive = false;
            currentSequence.clear();
            userInput.clear();
            targetPos = null;
            ticksRemaining = 0;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.literal("§cSequence Cancelled"), true);
            }
        }
    }
}