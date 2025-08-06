package com.spawnerfarm.modules;

import baritone.api.BaritoneAPI;
import baritone.api.process.IBaritoneProcess;
import baritone.api.pathing.goals.GoalBlock;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * SpawnerFarm Module - Automated skeleton spawner farming with enemy evasion
 * 
 * Features:
 * - Automatic spawner detection or manual coordinate input
 * - Silk Touch pickaxe verification
 * - Shift-mining for 64 spawner stacks
 * - Automatic storage in Ender Chest
 * - Enemy player detection and evasion protocol
 * - Complete Baritone integration for pathfinding
 */
public class SpawnerFarm extends Module {
    
    // Settings for the module - these appear in Meteor's GUI
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgCoordinates = settings.createGroup("Coordinates");
    private final SettingGroup sgEvasion = settings.createGroup("Evasion");
    
    // Manual coordinate input settings
    private final Setting<Boolean> useManualCoords = sgCoordinates.add(new BoolSetting.Builder()
        .name("manual-coordinates")
        .description("Use manually input coordinates instead of auto-detection.")
        .defaultValue(false)
        .build()
    );
    
    private final Setting<Integer> spawnerX = sgCoordinates.add(new IntSetting.Builder()
        .name("spawner-x")
        .description("X coordinate of the spawner.")
        .defaultValue(0)
        .visible(useManualCoords::get)
        .build()
    );
    
    private final Setting<Integer> spawnerY = sgCoordinates.add(new IntSetting.Builder()
        .name("spawner-y")
        .description("Y coordinate of the spawner.")
        .defaultValue(64)
        .range(0, 320)
        .sliderRange(0, 320)
        .visible(useManualCoords::get)
        .build()
    );
    
    private final Setting<Integer> spawnerZ = sgCoordinates.add(new IntSetting.Builder()
        .name("spawner-z")
        .description("Z coordinate of the spawner.")
        .defaultValue(0)
        .visible(useManualCoords::get)
        .build()
    );
    
    // General farming settings
    private final Setting<Integer> targetSpawnerCount = sgGeneral.add(new IntSetting.Builder()
        .name("target-spawners")
        .description("Number of spawners to collect before stopping. 0 = unlimited.")
        .defaultValue(64)
        .min(0)
        .build()
    );
    
    private final Setting<Integer> enderChestX = sgGeneral.add(new IntSetting.Builder()
        .name("ender-chest-x")
        .description("X coordinate of the Ender Chest.")
        .defaultValue(0)
        .build()
    );
    
    private final Setting<Integer> enderChestY = sgGeneral.add(new IntSetting.Builder()
        .name("ender-chest-y")
        .description("Y coordinate of the Ender Chest.")
        .defaultValue(64)
        .range(0, 320)
        .sliderRange(0, 320)
        .build()
    );
    
    private final Setting<Integer> enderChestZ = sgGeneral.add(new IntSetting.Builder()
        .name("ender-chest-z")
        .description("Z coordinate of the Ender Chest.")
        .defaultValue(0)
        .build()
    );
    
    // Evasion settings
    private final Setting<Double> threatRange = sgEvasion.add(new DoubleSetting.Builder()
        .name("threat-range")
        .description("Distance in blocks to consider enemy players a threat.")
        .defaultValue(20.0)
        .min(5.0)
        .max(100.0)
        .sliderMax(50.0)
        .build()
    );
    
    private final Setting<Boolean> enableEvasion = sgEvasion.add(new BoolSetting.Builder()
        .name("enable-evasion")
        .description("Enable automatic evasion when enemy players are detected.")
        .defaultValue(true)
        .build()
    );
    
    // Internal state variables
    private FarmState currentState = FarmState.IDLE;
    private BlockPos spawnerPos = null;
    private BlockPos enderChestPos = null;
    private boolean isMining = false;
    private boolean isEvading = false;
    private int collectedSpawners = 0;
    private long lastPlayerCheck = 0;
    
    // Farming state machine
    private enum FarmState {
        IDLE,                    // Waiting to start
        FINDING_SPAWNER,         // Looking for spawner location
        PATHING_TO_SPAWNER,      // Moving to spawner
        CHECKING_TOOLS,          // Verifying Silk Touch pickaxe
        MINING_SPAWNER,          // Breaking spawner with shift
        PATHING_TO_CHEST,        // Moving to Ender Chest
        STORING_ITEMS,           // Transferring spawners to chest
        COMPLETE,                // Farming complete
        EVADING                  // Emergency evasion mode
    }
    
    public SpawnerFarm() {
        super(new Category("Spawner Farm"), "spawner-farm", "Automated spawner farming with enemy evasion.");
    }
    
    /**
     * Called when the module is enabled
     * Initialize state and start the farming process
     */
    @Override
    public void onActivate() {
        if (mc.player == null || mc.world == null) {
            error("Cannot start farming - not in game!");
            toggle();
            return;
        }
        
        // Reset state
        currentState = FarmState.IDLE;
        spawnerPos = null;
        enderChestPos = null;
        isMining = false;
        isEvading = false;
        collectedSpawners = 0;
        
        // Set Ender Chest position
        enderChestPos = new BlockPos(enderChestX.get(), enderChestY.get(), enderChestZ.get());
        
        // Start the farming process
        if (useManualCoords.get()) {
            spawnerPos = new BlockPos(spawnerX.get(), spawnerY.get(), spawnerZ.get());
            info("Using manual spawner coordinates: " + spawnerPos.toShortString());
            currentState = FarmState.CHECKING_TOOLS;
        } else {
            info("Starting automatic spawner detection...");
            currentState = FarmState.FINDING_SPAWNER;
        }
        
        info("SpawnerFarm activated! Target: " + (targetSpawnerCount.get() == 0 ? "unlimited" : targetSpawnerCount.get()) + " spawners");
    }
    
    /**
     * Called when the module is disabled
     * Clean up and stop all processes
     */
    @Override
    public void onDeactivate() {
        // Cancel any active Baritone processes
        cancelBaritoneProcesses();
        
        // Release sneak key if held
        if (mc.options != null && mc.options.sneakKey.isPressed()) {
            mc.options.sneakKey.setPressed(false);
        }
        
        info("SpawnerFarm deactivated. Collected " + collectedSpawners + " spawners.");
    }
    
    /**
     * Main tick event handler - runs the farming state machine
     */
    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        
        // Check for enemy players every second (20 ticks)
        if (enableEvasion.get() && System.currentTimeMillis() - lastPlayerCheck > 1000) {
            checkForEnemyPlayers();
            lastPlayerCheck = System.currentTimeMillis();
        }
        
        // Handle evasion state
        if (isEvading) {
            handleEvasion();
            return;
        }
        
        // Run the main farming state machine
        switch (currentState) {
            case IDLE:
                // Module is active but not doing anything yet
                break;
                
            case FINDING_SPAWNER:
                findSpawner();
                break;
                
            case PATHING_TO_SPAWNER:
                checkPathingToSpawner();
                break;
                
            case CHECKING_TOOLS:
                if (checkSilkTouchPickaxe()) {
                    pathToSpawner();
                }
                break;
                
            case MINING_SPAWNER:
                handleMining();
                break;
                
            case PATHING_TO_CHEST:
                checkPathingToChest();
                break;
                
            case STORING_ITEMS:
                handleStorage();
                break;
                
            case COMPLETE:
                info("Farming complete! Collected " + collectedSpawners + " spawners.");
                toggle();
                break;
                
            case EVADING:
                // Handled above
                break;
        }
    }
    
    /**
     * Find the nearest spawner block automatically using Baritone
     */
    private void findSpawner() {
        try {
            // Use Baritone to find and mine spawner blocks
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            var mineProcess = baritone.getMineProcess();
            
            // Start mining process for spawners
            mineProcess.mineByName("minecraft:spawner");
            
            info("Baritone is searching for spawners...");
            currentState = FarmState.PATHING_TO_SPAWNER;
            
        } catch (Exception e) {
            error("Failed to start spawner detection: " + e.getMessage());
            toggle();
        }
    }
    
    /**
     * Check if Baritone has found and is pathing to a spawner
     */
    private void checkPathingToSpawner() {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        
        // Check if Baritone found a spawner and is pathing
        if (baritone.getPathingBehavior().isPathing()) {
            // Still pathing, wait
            return;
        }
        
        // Check if we're near a spawner
        spawnerPos = findNearbySpawner();
        if (spawnerPos != null) {
            info("Found spawner at: " + spawnerPos.toShortString());
            baritone.getMineProcess().cancel();
            currentState = FarmState.CHECKING_TOOLS;
        } else {
            error("No spawners found nearby!");
            toggle();
        }
    }
    
    /**
     * Find spawner blocks near the player
     */
    private BlockPos findNearbySpawner() {
        if (mc.world == null || mc.player == null) return null;
        
        BlockPos playerPos = mc.player.getBlockPos();
        int searchRadius = 10;
        
        // Search in a cube around the player
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    
                    if (block == Blocks.SPAWNER) {
                        return pos;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Verify the player has a Silk Touch pickaxe
     */
    private boolean checkSilkTouchPickaxe() {
        var inventory = mc.player.getInventory();
        
        // Check all inventory slots for a Silk Touch pickaxe
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            
            if (stack.getItem() instanceof PickaxeItem) {
                // Check if it has Silk Touch enchantment
                int silkTouchLevel = stack.getEnchantments().getLevel(Enchantments.SILK_TOUCH);
                if (silkTouchLevel > 0) {
                    // Select this pickaxe in hotbar
                    if (i < 9) {
                        inventory.selectedSlot = i;
                    } else {
                        // Move pickaxe to hotbar
                        moveItemToHotbar(i, stack);
                    }
                    info("Found Silk Touch pickaxe: " + stack.getName().getString());
                    return true;
                }
            }
        }
        
        error("No Silk Touch pickaxe found! Cannot mine spawners.");
        toggle();
        return false;
    }
    
    /**
     * Move an item from inventory to hotbar
     */
    private void moveItemToHotbar(int sourceSlot, ItemStack stack) {
        // Find first empty hotbar slot
        var inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getStack(i).isEmpty()) {
                // Swap items
                if (mc.interactionManager != null) {
                    mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        sourceSlot,
                        i,
                        SlotActionType.SWAP,
                        mc.player
                    );
                }
                inventory.selectedSlot = i;
                break;
            }
        }
    }
    
    /**
     * Use Baritone to path to the spawner location
     */
    private void pathToSpawner() {
        if (spawnerPos == null) {
            error("No spawner position set!");
            toggle();
            return;
        }
        
        try {
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            var goalProcess = baritone.getCustomGoalProcess();
            
            // Set goal to the spawner position
            goalProcess.setGoalAndPath(new GoalBlock(spawnerPos));
            
            info("Pathing to spawner at: " + spawnerPos.toShortString());
            currentState = FarmState.PATHING_TO_SPAWNER;
            
        } catch (Exception e) {
            error("Failed to path to spawner: " + e.getMessage());
            toggle();
        }
    }
    
    /**
     * Handle the mining process with shift-clicking for 64 spawners
     */
    private void handleMining() {
        if (spawnerPos == null || mc.player == null || mc.interactionManager == null) {
            return;
        }
        
        double distanceToSpawner = mc.player.squaredDistanceTo(
            spawnerPos.getX() + 0.5, spawnerPos.getY() + 0.5, spawnerPos.getZ() + 0.5
        );
        
        // If we're close enough to the spawner, start mining
        if (distanceToSpawner <= 6.0) { // Within reach
            if (!isMining) {
                // Start mining process
                info("Starting to mine spawner with shift for 64 spawners...");
                
                // Hold shift for 64 spawner stack
                mc.options.sneakKey.setPressed(true);
                
                // Start breaking the block
                mc.interactionManager.attackBlock(spawnerPos, Direction.UP);
                isMining = true;
            }
            
            // Check if spawner is broken
            if (mc.world.getBlockState(spawnerPos).getBlock() != Blocks.SPAWNER) {
                // Spawner broken successfully
                mc.options.sneakKey.setPressed(false);
                isMining = false;
                
                // Count spawners in inventory
                countSpawnersInInventory();
                
                info("Mined spawner! Total collected: " + collectedSpawners);
                
                // Check if we've reached target
                if (targetSpawnerCount.get() > 0 && collectedSpawners >= targetSpawnerCount.get()) {
                    currentState = FarmState.PATHING_TO_CHEST;
                } else {
                    // Look for more spawners or path to chest
                    currentState = FarmState.PATHING_TO_CHEST;
                }
            }
        } else {
            // Too far from spawner, path closer
            pathToSpawner();
        }
    }
    
    /**
     * Count spawners currently in the player's inventory
     */
    private void countSpawnersInInventory() {
        if (mc.player == null) return;
        
        var inventory = mc.player.getInventory();
        collectedSpawners = 0;
        
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.SPAWNER) {
                collectedSpawners += stack.getCount();
            }
        }
    }
    
    /**
     * Check if Baritone is still pathing to the chest
     */
    private void checkPathingToChest() {
        var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
        
        if (!baritone.getPathingBehavior().isPathing()) {
            // Reached chest or path failed
            double distanceToChest = mc.player.squaredDistanceTo(
                enderChestPos.getX() + 0.5, enderChestPos.getY() + 0.5, enderChestPos.getZ() + 0.5
            );
            
            if (distanceToChest <= 6.0) {
                currentState = FarmState.STORING_ITEMS;
            } else {
                // Path to chest
                pathToEnderChest();
            }
        }
    }
    
    /**
     * Path to the Ender Chest using Baritone
     */
    private void pathToEnderChest() {
        try {
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            var goalProcess = baritone.getCustomGoalProcess();
            
            goalProcess.setGoalAndPath(new GoalBlock(enderChestPos));
            
            info("Pathing to Ender Chest at: " + enderChestPos.toShortString());
            currentState = FarmState.PATHING_TO_CHEST;
            
        } catch (Exception e) {
            error("Failed to path to Ender Chest: " + e.getMessage());
            toggle();
        }
    }
    
    /**
     * Handle storage of spawners in the Ender Chest
     */
    private void handleStorage() {
        if (enderChestPos == null || mc.player == null || mc.interactionManager == null) {
            return;
        }
        
        // Check if we're close enough to the chest
        double distanceToChest = mc.player.squaredDistanceTo(
            enderChestPos.getX() + 0.5, enderChestPos.getY() + 0.5, enderChestPos.getZ() + 0.5
        );
        
        if (distanceToChest <= 6.0) {
            // Open the Ender Chest
            BlockHitResult hitResult = new BlockHitResult(
                new Vec3d(enderChestPos.getX() + 0.5, enderChestPos.getY() + 0.5, enderChestPos.getZ() + 0.5),
                Direction.UP,
                enderChestPos,
                false
            );
            
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            
            // Wait a few ticks for GUI to open, then transfer items
            transferSpawnersToChest();
            
            info("Stored spawners in Ender Chest!");
            currentState = FarmState.COMPLETE;
            
        } else {
            // Too far, path closer
            pathToEnderChest();
        }
    }
    
    /**
     * Transfer all spawners from player inventory to open Ender Chest
     */
    private void transferSpawnersToChest() {
        if (mc.player == null || mc.player.currentScreenHandler == null) {
            return;
        }
        
        ScreenHandler screenHandler = mc.player.currentScreenHandler;
        var inventory = mc.player.getInventory();
        
        // Find all spawners in player inventory and move them
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.SPAWNER) {
                // Move spawner stack to chest
                if (mc.interactionManager != null) {
                    mc.interactionManager.clickSlot(
                        screenHandler.syncId,
                        i,
                        0,
                        SlotActionType.QUICK_MOVE,
                        mc.player
                    );
                }
            }
        }
        
        // Close the chest
        mc.player.closeHandledScreen();
    }
    
    /**
     * Check for enemy players within threat range
     */
    private void checkForEnemyPlayers() {
        if (mc.world == null || mc.player == null) return;
        
        List<? extends PlayerEntity> players = mc.world.getPlayers();
        
        for (PlayerEntity player : players) {
            // Skip our own player
            if (player == mc.player) continue;
            
            // Calculate distance to player
            double distance = mc.player.distanceTo(player);
            
            // Check if player is within threat range
            if (distance <= threatRange.get()) {
                warning("Enemy player detected: " + player.getName().getString() + 
                       " at distance " + String.format("%.1f", distance) + " blocks!");
                
                // Trigger evasion protocol
                triggerEvasion();
                return;
            }
        }
    }
    
    /**
     * Trigger the emergency evasion protocol
     */
    private void triggerEvasion() {
        if (isEvading) return; // Already evading
        
        warning("EMERGENCY EVASION TRIGGERED!");
        isEvading = true;
        currentState = FarmState.EVADING;
        
        // Cancel all Baritone processes immediately
        cancelBaritoneProcesses();
        
        // Release sneak key if held
        if (mc.options.sneakKey.isPressed()) {
            mc.options.sneakKey.setPressed(false);
        }
        
        // Complete any mining in progress
        if (isMining) {
            isMining = false;
        }
    }
    
    /**
     * Handle the evasion sequence
     */
    private void handleEvasion() {
        // Step 1: Store any spawners we have
        storeSpawnersEmergency();
        
        // Step 2: Disconnect from server
        warning("Disconnecting for safety!");
        if (mc.world != null) {
            mc.disconnect();
        }
        
        // Disable the module
        toggle();
    }
    
    /**
     * Emergency storage of spawners (simplified version)
     */
    private void storeSpawnersEmergency() {
        if (enderChestPos == null || mc.player == null) return;
        
        double distanceToChest = mc.player.squaredDistanceTo(
            enderChestPos.getX() + 0.5, enderChestPos.getY() + 0.5, enderChestPos.getZ() + 0.5
        );
        
        // If we're close to chest, try to store quickly
        if (distanceToChest <= 6.0) {
            // Quick attempt to open and store
            BlockHitResult hitResult = new BlockHitResult(
                new Vec3d(enderChestPos.getX() + 0.5, enderChestPos.getY() + 0.5, enderChestPos.getZ() + 0.5),
                Direction.UP,
                enderChestPos,
                false
            );
            
            if (mc.interactionManager != null) {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            }
        }
    }
    
    /**
     * Cancel all active Baritone processes
     */
    private void cancelBaritoneProcesses() {
        try {
            var baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
            
            // Cancel all processes
            baritone.getMineProcess().cancel();
            baritone.getCustomGoalProcess().cancel();
            baritone.getPathingBehavior().cancelEverything();
            
        } catch (Exception e) {
            error("Failed to cancel Baritone processes: " + e.getMessage());
        }
    }
    
    /**
     * Send info message to player
     */
    private void info(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§a[SpawnerFarm] " + message), false);
        }
    }
    
    /**
     * Send warning message to player
     */
    private void warning(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§e[SpawnerFarm] " + message), false);
        }
    }
    
    /**
     * Send error message to player
     */
    private void error(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§c[SpawnerFarm] " + message), false);
        }
    }
}