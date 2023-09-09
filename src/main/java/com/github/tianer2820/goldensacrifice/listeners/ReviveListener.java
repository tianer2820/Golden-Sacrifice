package com.github.tianer2820.goldensacrifice.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.github.tianer2820.goldensacrifice.GoldenSacrifice;
import com.github.tianer2820.goldensacrifice.constants.CommonConstants;
import com.github.tianer2820.goldensacrifice.items.UndeadPotion;
import com.github.tianer2820.goldensacrifice.items.UndeadPotion_splash;


public class ReviveListener implements Listener {
    private Boolean debug = false;
    private void debuglog(Player p, String msg){
        if(debug){
            p.sendMessage(msg);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event){
        ItemStack i = event.getItem();
        Player p = event.getPlayer();
        debuglog(p, event.getEventName());
        if(UndeadPotion.isItem(i)){
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, Integer.MAX_VALUE, 0));
            
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event){
        Player p = event.getPlayer();
        debuglog(p, event.getEventName());
        PotionEffect pe = p.getPotionEffect(PotionEffectType.WEAKNESS);
        if(pe == null) return;
        if(pe.getDuration() < 1000000000) return;

        Block blk = event.getClickedBlock();
        
        if (blk.getType() == Material.PLAYER_HEAD || blk.getType() == Material.PLAYER_WALL_HEAD) {
            debuglog(p, "A PLAYER HEAD");
            Skull skull = (Skull) blk.getState();
            if (skull.hasOwner()) {
                OfflinePlayer offlinePlayer = skull.getOwningPlayer();
                Player rp = null;
                if(offlinePlayer.isOnline()){
                    rp = (Player)offlinePlayer;
                }
                if(rp == null) return;
                debuglog(p, "PLAYER ONLINE, REVIVING");
                rp.setGameMode(GameMode.SURVIVAL);
                rp.teleport(rp.getBedSpawnLocation());
                zombiefy(rp);
                p.removePotionEffect(PotionEffectType.WEAKNESS);
            }
        } else {
            debuglog(p, "NOT A PLAYER HEAD");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event){
        ProjectileSource ps = event.getEntity().getShooter();
        Player p = (ps instanceof Player) ? (Player)ps : null;
        debuglog(p, event.getEventName());
        if(UndeadPotion_splash.isItem(event.getPotion().getItem())){
            debuglog(p, "THROWN UNDEAD POTION");
            // Block blk = event.getHitBlock();
            // Entity en = null;
            // if(blk == null) {
            //     en = event.getHitEntity();
            // }
            // if(en == null){
            //     debuglog(p, "WTF DID YOU HIT??");
            //     return;
            // }
            ArrayList<Entity> ets = (ArrayList<Entity>) event.getPotion().getLocation().getNearbyEntities(2, 2, 2);
            if(ets != null){
                debuglog(p, "FOUND ENTITIES NEARBY");
            }
            // detect playerhead items nearby
            ArrayList<Item> pl = new ArrayList<>();
            for(Entity et : ets){
                if(et instanceof Item){
                    if(((Item)et).getItemStack().getType() == Material.PLAYER_HEAD){
                        debuglog(p, "FOUND PLAYER HEAD");
                        pl.add((Item)et);
                    }
                }
            }

            // detect playerhead blocks nearby
            ArrayList<Block> headblks = new ArrayList<>();
            Location loc = event.getPotion().getLocation();
            for(int i = -2; i < 3; i++){
                for(int j = -2; j < 3; j++){
                    for(int k = -2; k < 3; k++){
                        Block blk = loc.add(i, j, k).getBlock();
                        if(blk.getType() == Material.PLAYER_HEAD || blk.getType() == Material.PLAYER_WALL_HEAD){
                            headblks.add(blk);
                        }
                    }
                }
            }


            for(Item it : pl){
                SkullMeta meta = (SkullMeta) it.getItemStack().getItemMeta();
                OfflinePlayer offlinePlayer = meta.getOwningPlayer();
                Player rp = null;
                if(offlinePlayer.isOnline()){
                    rp = (Player)offlinePlayer;
                }
                if(rp == null) return;
                debuglog(p, "PLAYER ONLINE, REVIVING");
                rp.setGameMode(GameMode.SURVIVAL);
                rp.teleport(rp.getBedSpawnLocation());
                zombiefy(rp);
            }

            for(Block bl : headblks){
                Skull skull = (Skull) bl.getState();
                if (skull.hasOwner()) {
                    OfflinePlayer offlinePlayer = skull.getOwningPlayer();
                    Player rp = null;
                    if(offlinePlayer.isOnline()){
                        rp = (Player)offlinePlayer;
                    }
                    if(rp == null) return;
                    debuglog(p, "PLAYER ONLINE, REVIVING");
                    rp.setGameMode(GameMode.SURVIVAL);
                    rp.teleport(rp.getBedSpawnLocation());
                    zombiefy(rp);
                    p.removePotionEffect(PotionEffectType.WEAKNESS);
                }
            }

        }
    }

    private void zombiefy(Player p){
        World w = Bukkit.getWorlds().get(0);
        NamespacedKey nKey = new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString());
        w.getPersistentDataContainer().set(nKey, PersistentDataType.BOOLEAN, true);

        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, Integer.MAX_VALUE, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 2));
        // debuglog(p, "YOU ARE ZOMBIEFIED!");
        p.sendMessage("YOU ARE ZOMBIEFIED!");
    }


    @EventHandler(ignoreCancelled = true)
    public void onHeal(EntityRegainHealthEvent event){
        Player p = null;
        if(event.getEntity() instanceof Player){
            p = (Player)event.getEntity();
        }
        if(p == null) return;
        debuglog(p, event.getEventName());
        if(Bukkit.getWorlds().get(0).getPersistentDataContainer().get(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN) == true){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event){
        Player p = event.getPlayer();
        debuglog(p, event.getEventName());
        if(Bukkit.getWorlds().get(0).getPersistentDataContainer().get(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN) == true){
            Bukkit.getWorlds().get(0).getPersistentDataContainer().set(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN, false);
        }
    }
}
