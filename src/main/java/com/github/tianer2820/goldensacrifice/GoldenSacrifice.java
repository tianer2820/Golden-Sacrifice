package com.github.tianer2820.goldensacrifice;



import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.tianer2820.goldensacrifice.constants.CommonConstants;
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
    }

    private void registerRecipies(){
    }
}
