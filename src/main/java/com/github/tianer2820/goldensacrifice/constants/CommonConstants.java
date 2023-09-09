package com.github.tianer2820.goldensacrifice.constants;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class CommonConstants {

    public static final String ITEM_ID = "item_id";
    public static NamespacedKey ITEM_ID_KEY;

    public static final String EXAMPLE_ITEM = "example_item";
    public static NamespacedKey NORMAL_STICK_RECIPE_KEY = null;

    public static final String UNDEAD_POTION = "UNDEAD_POTION";
    public static NamespacedKey UNDEAD_POTION_RECIPE_KEY = null;

    public static final String UNDEAD_POTION_SPLASH = "UNDEAD_POTION_SPLASH";
    public static NamespacedKey UNDEAD_POTION_SPLASH_RECIPE_KEY = null;

    public static void initializeConstants(Plugin plugin){
        ITEM_ID_KEY = new NamespacedKey(plugin, ITEM_ID);
        NORMAL_STICK_RECIPE_KEY = new NamespacedKey(plugin, EXAMPLE_ITEM);
        UNDEAD_POTION_RECIPE_KEY = new NamespacedKey(plugin, UNDEAD_POTION);
        UNDEAD_POTION_SPLASH_RECIPE_KEY = new NamespacedKey(plugin, UNDEAD_POTION_SPLASH);
    }
}
