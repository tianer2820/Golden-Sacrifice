package com.github.tianer2820.goldensacrifice.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.tianer2820.goldensacrifice.GoldenSacrifice;
import com.github.tianer2820.goldensacrifice.constants.BlockCollectionConstants;
import com.github.tianer2820.goldensacrifice.constants.ConfigConstants;
import com.google.common.collect.ImmutableSet;

public class SacrificeHelpers {
    private static final Set<Block> protectedAltarBlocks = new HashSet<>();

    public static boolean isValidAltar(Block headBlock){
        return !detectValidAltar(headBlock).isEmpty();
    }

    public static Set<Block> detectValidAltar(Block headBlock){
        if(!ImmutableSet.of(Material.PLAYER_HEAD, Material.PLAYER_WALL_HEAD).contains(headBlock.getType())){
            return Collections.emptySet();
        }
        if(protectedAltarBlocks.contains(headBlock)){
            // already has an ongoing sacrifice, ignore
            return Collections.emptySet();
        }

        Set<Block> detectedBlocks = new HashSet<>();
        detectedBlocks.add(headBlock);

        // check if the blocks under are all stones
        Block under = headBlock.getRelative(BlockFace.DOWN);
        for (int h = 0; h <= 1; h++) {
            Set<Block> layer = detectValidSquare(under.getRelative(0, -h, 0), h, Material.COBBLESTONE);
            if(layer.isEmpty()){
                return Collections.emptySet();
            }
            detectedBlocks.addAll(layer);
        }
        return detectedBlocks;
    }

    public static boolean tryBeginSacrifice(Block headBlock){
        GoldenSacrifice.getInstance().getLogger().info(String.format("Try begin sacrifice at %d,%d,%d", headBlock.getX(), headBlock.getY(), headBlock.getZ()));
        // detect valid altar
        Set<Block> altarBlocks = detectValidAltar(headBlock);
        if(altarBlocks.isEmpty()){
            GoldenSacrifice.getInstance().getLogger().info("Not a valid altar");
            return false;
        }        
        // detect valid player
        Skull skull = (Skull)headBlock.getState();
        OfflinePlayer offlinePlayer = skull.getOwningPlayer();
        if(offlinePlayer == null){
            GoldenSacrifice.getInstance().getLogger().info("Skull does not have player info");
            return false;
        }
        Player player = offlinePlayer.getPlayer();
        if(player == null){
            GoldenSacrifice.getInstance().getLogger().info("Player not online");
            return false;
        }
        if(player.getGameMode() == GameMode.SURVIVAL){
            GoldenSacrifice.getInstance().getLogger().info("Player already in survival mode");
            return false;
        }

        // begin the sacrifice process
        GoldenSacrifice.getInstance().getLogger().info("Sacrifice task started");
        protectedAltarBlocks.addAll(altarBlocks);
        new SacrificeRunnable(headBlock, player, altarBlocks).runTaskTimer(GoldenSacrifice.getInstance(), 0, 20);
        return true;
    }

    public static boolean isProtectedBlock(Block block){
        return protectedAltarBlocks.contains(block);
    }

    /**
     * Detect if a NxN square are all made by the same block type
     * radius=1 means 3x3, radius=2 means 5x5, etc.
     */
    private static Set<Block> detectValidSquare(Block center, int radius, Material blockType){
        Set<Block> blocks = new HashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block block = center.getRelative(dx, 0, dz);
                if(block.getType() != blockType){
                    return Collections.emptySet();
                }
                blocks.add(block);
            }
        }
        return blocks;
    }


    /**
     * The background sacrifice process
     */
    private static class SacrificeRunnable extends BukkitRunnable{
        private final Random rng = new Random();

        private Block headBlock;
        private Player player;
        Set<Block> altarBlocks;

        private int progress = 0;
        private int energyCollected = 0;

        public SacrificeRunnable(Block headBlock, Player player, Set<Block> altarBlocks){
            this.headBlock = headBlock;
            this.player = player;
            this.altarBlocks = altarBlocks;
        }

        @Override
        public void run() {
            // check if player is still online
            if (!player.isValid()){
                cancel();
                protectedAltarBlocks.removeAll(altarBlocks);
            }

            // limit the range
            progress += 1;
            if(progress > ConfigConstants.ENERGY_SEARCH_RANGE_LIMIT){
                cancel();
                protectedAltarBlocks.removeAll(altarBlocks);
            }
            
            // check each block
            getCylinderShellLocations(headBlock.getLocation(), progress).forEach(location -> {
                Block block = location.getBlock();
                Material blockType = block.getType();

                // cumulate energy
                int energy = BlockCollectionConstants.MATERIAL_TO_ENERGY_MAP.getOrDefault(blockType, 0);
                if(energy > 0){
                    block.setType(Material.AIR);
                    energyCollected += energy;
                }

                // replace other blocks
                else if(BlockCollectionConstants.MATERIALS_TO_BE_REPLACED.contains(blockType)){
                    // decide if replace happens
                    double chance = 10 / (block.getLocation().distance(headBlock.getLocation()) + 5);
                    if (rng.nextDouble() < chance){
                        int idx = rng.nextInt(BlockCollectionConstants.WITHERED_MATERIALS.size());
                        block.setType(BlockCollectionConstants.WITHERED_MATERIALS.get(idx));
                    }
                }

                // generate pillars
                if(block.isSolid() && !(block.getRelative(0, 1, 0).isSolid())){
                    Block above = block.getRelative(0, 1, 0);
                    double distance = block.getLocation().distance(headBlock.getLocation());
                    double chance = 3.0 / (distance + 10) - 0.02;
                    if (rng.nextDouble() < chance && distance >= 6){
                        // should make a pillar
                        int height = (int)(chance * 60) + 1;
                        makePillar(above, height, Material.OBSIDIAN);
                    }
                }
            });

            // respawn if energy is full
            if(energyCollected >= ConfigConstants.SACRIFICE_ENERGY_NEEDED && player.isValid()){
                // do respawn player
                headBlock.setType(Material.AIR);
                player.teleport(headBlock.getLocation());
                player.setGameMode(GameMode.SURVIVAL);
                cancel();
                protectedAltarBlocks.removeAll(altarBlocks);
            }
        }

        private void makePillar(Block base, int height, Material blockType){
            for (int i = 0; i < height; i++) {
                base.getRelative(0, i, 0).setType(blockType);
                base.getRelative(0, -i, 0).setType(blockType);
            }
        }

        private Set<Location> getCylinderShellLocations(Location center, int radius){
            Set<Location> locations = new HashSet<>();
            int cx = center.getBlockX();
            int cy = center.getBlockY();
            int cz = center.getBlockZ();

            for (int dx = 0; dx <= radius * 0.7071; dx++) {
                int dz = (int)Math.sqrt(Math.pow(radius, 2) - Math.pow(dx, 2));
                
                // wall
                for (int dy = -radius; dy < radius; dy++) {
                    locations.add(new Location(center.getWorld(), cx + dx, cy + dy, cz + dz));
                    locations.add(new Location(center.getWorld(), cx - dx, cy + dy, cz + dz));
                    locations.add(new Location(center.getWorld(), cx + dx, cy + dy, cz - dz));
                    locations.add(new Location(center.getWorld(), cx - dx, cy + dy, cz - dz));

                    locations.add(new Location(center.getWorld(), cx + dz, cy + dy, cz + dx));
                    locations.add(new Location(center.getWorld(), cx - dz, cy + dy, cz + dx));
                    locations.add(new Location(center.getWorld(), cx + dz, cy + dy, cz - dx));
                    locations.add(new Location(center.getWorld(), cx - dz, cy + dy, cz - dx));

                }

                // top & bottom
                for (int i = 0; i < dz; i++) {
                    // top
                    locations.add(new Location(center.getWorld(), cx + dx, cy + radius, cz + (dz-i)));
                    locations.add(new Location(center.getWorld(), cx - dx, cy + radius, cz + (dz-i)));
                    locations.add(new Location(center.getWorld(), cx + dx, cy + radius, cz - (dz-i)));
                    locations.add(new Location(center.getWorld(), cx - dx, cy + radius, cz - (dz-i)));

                    locations.add(new Location(center.getWorld(), cx + (dz-i), cy + radius, cz + dx));
                    locations.add(new Location(center.getWorld(), cx - (dz-i), cy + radius, cz + dx));
                    locations.add(new Location(center.getWorld(), cx + (dz-i), cy + radius, cz - dx));
                    locations.add(new Location(center.getWorld(), cx - (dz-i), cy + radius, cz - dx));

                    // bottom
                    locations.add(new Location(center.getWorld(), cx + dx, cy - radius, cz + (dz-i)));
                    locations.add(new Location(center.getWorld(), cx - dx, cy - radius, cz + (dz-i)));
                    locations.add(new Location(center.getWorld(), cx + dx, cy - radius, cz - (dz-i)));
                    locations.add(new Location(center.getWorld(), cx - dx, cy - radius, cz - (dz-i)));

                    locations.add(new Location(center.getWorld(), cx + (dz-i), cy - radius, cz + dx));
                    locations.add(new Location(center.getWorld(), cx - (dz-i), cy - radius, cz + dx));
                    locations.add(new Location(center.getWorld(), cx + (dz-i), cy - radius, cz - dx));
                    locations.add(new Location(center.getWorld(), cx - (dz-i), cy - radius, cz - dx));
                }
            }

            return locations;
        }
    }
}
