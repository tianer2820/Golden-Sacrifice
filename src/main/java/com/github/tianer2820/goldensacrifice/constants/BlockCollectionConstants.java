package com.github.tianer2820.goldensacrifice.constants;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class BlockCollectionConstants {
    // each block type can provide different energy level for the golden sacrifice process
    public static final Map<Material, Integer> MATERIAL_TO_ENERGY_MAP = new ImmutableMap.Builder<Material, Integer>()
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

    // these blocks will be replaced when the sacrifice task search for energy
    public static final Set<Material> MATERIALS_TO_BE_REPLACED = new ImmutableSet.Builder<Material>()
        .add(Material.GRASS_BLOCK)
        .add(Material.FARMLAND)
        .build();
        
    // these are chosen randomly to replace the above blocks
    public static final List<Material> WITHERED_MATERIALS = new ImmutableList.Builder<Material>()
        .add(Material.SAND)
        .add(Material.GRAVEL)
        .add(Material.COBBLESTONE)
        .add(Material.COARSE_DIRT)
        .build();
}
