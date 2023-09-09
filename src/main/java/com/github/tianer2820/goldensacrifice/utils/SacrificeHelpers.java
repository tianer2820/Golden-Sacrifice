package com.github.tianer2820.goldensacrifice.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class SacrificeHelpers {
    private static final Map<Material, Integer> MATERIAL_TO_ENERGY_MAP = new ImmutableMap.Builder<Material, Integer>()
        // Leaves
        .put(Material.OAK_LEAVES, 1)
        .put(Material.SPRUCE_LEAVES, 1)
        .put(Material.BIRCH_LEAVES, 1)
        .put(Material.JUNGLE_LEAVES, 1)
        .put(Material.ACACIA_LEAVES, 1)
        .put(Material.CHERRY_LEAVES, 1)
        .put(Material.DARK_OAK_LEAVES, 1)
        .put(Material.MANGROVE_LEAVES, 1)
        .put(Material.AZALEA_LEAVES, 1)
        .put(Material.FLOWERING_AZALEA_LEAVES, 1)
        // Crops
        .put(Material.WHEAT, 2)
        .put(Material.CARROT, 2)
        .put(Material.POTATO, 2)
        // Special blocks
        .put(Material.HAY_BLOCK, 2)
        // Add more
        .build();

    // these blocks will be replaced
    private static final Set<Material> MATERIALS_TO_REPLACE = new ImmutableSet.Builder<Material>()
        .add(Material.GRASS_BLOCK)
        .add(Material.FARMLAND)
        .build();
    // these are choosen randomly to replace the above blocks
    private static final List<Material> REPLACE_POOL = new ImmutableList.Builder<Material>()
        .add(Material.SAND)
        .add(Material.GRAVEL)
        .add(Material.COBBLESTONE)
        .add(Material.COARSE_DIRT)
        .build();

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
        if(player.getGameMode() != GameMode.SPECTATOR){
            GoldenSacrifice.getInstance().getLogger().info("Player not in spectator mode");
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

        private static final int ENERGY_NEEDED = 128;
        private static final int RANGE_LIMIT = 32;

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
            if(progress > RANGE_LIMIT){
                cancel();
                protectedAltarBlocks.removeAll(altarBlocks);
            }
            
            // check each block
            getCubeShellLocations(headBlock.getLocation(), progress).forEach(location -> {
                Block block = location.getBlock();
                Material blockType = block.getType();

                // cumulate energy
                int energy = MATERIAL_TO_ENERGY_MAP.getOrDefault(blockType, 0);
                if(energy > 0){
                    block.setType(Material.AIR);
                    energyCollected += energy;
                }

                // replace other blocks
                else if(MATERIALS_TO_REPLACE.contains(blockType)){
                    int idx = rng.nextInt(REPLACE_POOL.size());
                    block.setType(REPLACE_POOL.get(idx));
                }
            });

            // respawn if energy is full
            if(energyCollected >= ENERGY_NEEDED && player.isValid()){
                // do respawn player
                headBlock.setType(Material.AIR);
                player.teleport(headBlock.getLocation());
                player.setGameMode(GameMode.SURVIVAL);
                cancel();
                protectedAltarBlocks.removeAll(altarBlocks);
            }
        }

        private Set<Location> getCubeShellLocations(Location center, int radius){
            Set<Location> locations = new HashSet<>();
            int cx = center.getBlockX();
            int cy = center.getBlockY();
            int cz = center.getBlockZ();

            // top and bottom cap
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    locations.add(new Location(center.getWorld(), cx + dx, cy + radius, cz + dz));
                    locations.add(new Location(center.getWorld(), cx + dx, cy - radius, cz + dz));
                }
            }
            // walls
            for (int h = -(radius - 1); h <= (radius - 1); h++) {
                int layer = center.getBlockY() + h;

                for (int i = -radius; i < radius; i++) {
                    locations.add(new Location(center.getWorld(), cx + i, layer, cz - radius));
                }
                for (int i = -radius; i < radius; i++) {
                    locations.add(new Location(center.getWorld(), cx + radius, layer, cz + i));
                }
                for (int i = -radius; i < radius; i++) {
                    locations.add(new Location(center.getWorld(), cx - i, layer, cz + radius));
                }
                for (int i = -radius; i < radius; i++) {
                    locations.add(new Location(center.getWorld(), cx - radius, layer, cz - i));
                }
            }
            return locations;
        }

    }
}
