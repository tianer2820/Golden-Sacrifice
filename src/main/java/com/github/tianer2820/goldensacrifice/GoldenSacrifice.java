package com.github.tianer2820.goldensacrifice;



import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.tianer2820.goldensacrifice.constants.CommonConstants;
import com.github.tianer2820.goldensacrifice.items.UndeadPotion;
import com.github.tianer2820.goldensacrifice.listeners.RecallListener;
import com.github.tianer2820.goldensacrifice.items.UndeadPotionSplash;
import com.github.tianer2820.goldensacrifice.listeners.ReviveListener;
import com.github.tianer2820.goldensacrifice.listeners.SacrificeListener;


public class GoldenSacrifice extends JavaPlugin implements Listener{
    static private GoldenSacrifice instance;


    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        CommonConstants.initializeConstants(this);

        registerCommands();
        registerListeners();
        registerRecipies();

        startBgTasks();
    }

    public static GoldenSacrifice getInstance(){
        return instance;
    }

    private void startBgTasks(){
    }
    
    private void registerCommands(){
    }

    private void registerListeners(){
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new SacrificeListener(), this);
        manager.registerEvents(new ReviveListener(), this);
        manager.registerEvents(new RecallListener(), this);
    }

    private void registerRecipies(){
        // undead potion
        ShapedRecipe recipe = new ShapedRecipe(CommonConstants.UNDEAD_POTION_RECIPE_KEY, UndeadPotion.getItemStack(1));
        recipe.shape(
                "P",
                "S");
        recipe.setIngredient('P', Material.POTION);
        recipe.setIngredient('S', Material.SOUL_SAND);
        getServer().addRecipe(recipe);

        // undead potion splash
        ShapedRecipe recipe2 = new ShapedRecipe(CommonConstants.UNDEAD_POTION_SPLASH_RECIPE_KEY, UndeadPotionSplash.getItemStack(1));
        recipe2.shape(
                "P",
                "S");
        recipe2.setIngredient('P', Material.SPLASH_POTION);
        recipe2.setIngredient('S', Material.SOUL_SAND);
        getServer().addRecipe(recipe2);
    }
}
