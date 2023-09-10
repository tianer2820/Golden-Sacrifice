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
import org.bukkit.entity.Creature;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Witch;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import com.github.tianer2820.goldensacrifice.GoldenSacrifice;
import com.github.tianer2820.goldensacrifice.items.UndeadPotion;
import com.github.tianer2820.goldensacrifice.items.UndeadPotionSplash;


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
        PotionEffect pe = p.getPotionEffect(PotionEffectType.HUNGER);
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
                // Location spawn = (rp.getBedSpawnLocation() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : rp.getBedSpawnLocation();
                // rp.teleport(spawn);
                rp.teleport(blk.getLocation());
                blk.setType(Material.AIR);
                zombiefy(rp);
                p.removePotionEffect(PotionEffectType.HUNGER);
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
        if(UndeadPotionSplash.isItem(event.getPotion().getItem())){
            debuglog(p, "THROWN UNDEAD POTION");

            ArrayList<Entity> ets = (ArrayList<Entity>) event.getPotion().getLocation().getNearbyEntities(1, 1, 1);
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
            for(int i = -1; i < 2; i++){
                for(int j = -1; j < 2; j++){
                    for(int k = -1; k < 2; k++){
                        Block blk = loc.clone().add(i, j, k).getBlock();
                        if(blk.getType() == Material.PLAYER_HEAD || blk.getType() == Material.PLAYER_WALL_HEAD){
                            debuglog(p, "FOUND PLAYER HEAD");
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
                // Location spawn = (rp.getBedSpawnLocation() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : rp.getBedSpawnLocation();
                // rp.teleport(spawn);
                rp.teleport(it.getLocation());
                it.remove();
                
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
                    // Location spawn = (rp.getBedSpawnLocation() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : rp.getBedSpawnLocation();
                    // rp.teleport(spawn);
                    rp.teleport(bl.getLocation());
                    bl.setType(Material.AIR);
                    zombiefy(rp);
                    p.removePotionEffect(PotionEffectType.HUNGER);
                }
            }

        }
    }

    private void zombiefy(Player p){
        World w = Bukkit.getWorlds().get(0);
        NamespacedKey nKey = new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString());
        w.getPersistentDataContainer().set(nKey, PersistentDataType.BOOLEAN, true);

        p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, true, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 2, true, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, Integer.MAX_VALUE, 2, true, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, Integer.MAX_VALUE, 2, true, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, true, true, true));
        p.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 2, true, true, true));
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
        if(Bukkit.getWorlds().get(0).getPersistentDataContainer().getOrDefault(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN, false) == true){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event){
        Player p = event.getPlayer();
        debuglog(p, event.getEventName());
        if(Bukkit.getWorlds().get(0).getPersistentDataContainer().getOrDefault(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN, false) == true){
            Bukkit.getWorlds().get(0).getPersistentDataContainer().set(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN, false);
        }
    }

    private enum Hostiles{ // for zombiefied players
        IRONGOLEM(IronGolem.class),
        SNOWMAN(Snowman.class),
        VINDICATOR(Vindicator.class),
        EVOKER(Evoker.class),
        WITCH(Witch.class),
        PILLAGER(Pillager.class),
        GUARGIAN(Guardian.class),
        ELDERGUARDIAN(ElderGuardian.class),
        DROWNED(Drowned.class),
        WITHERSKELETON(WitherSkeleton.class);

        private final Class<? extends Entity> mobClass;

        Hostiles(Class<? extends Entity> mobClass) {
            this.mobClass = mobClass;
        }

        public Class<? extends Entity> getMobClass() {
            return mobClass;
        }
        public static Boolean contains(Entity e){
            for(Hostiles hostile : Hostiles.values()){
                if(hostile.getMobClass().isInstance(e)){
                    return true;
                }
            }
            return false;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event){
        if(event.getTarget() instanceof Player){
            Player p = (Player)event.getTarget();
            debuglog(p, event.getEventName());
            if(!Hostiles.contains(event.getEntity())){
                if(Bukkit.getWorlds().get(0).getPersistentDataContainer().getOrDefault(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN, false) == true){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event){
        Player p = event.getPlayer();
        if(Bukkit.getWorlds().get(0).getPersistentDataContainer().getOrDefault(new NamespacedKey(GoldenSacrifice.getInstance(), p.getUniqueId().toString()), PersistentDataType.BOOLEAN, false) == true){
            ArrayList<LivingEntity> ents = (ArrayList<LivingEntity>)p.getLocation().getNearbyLivingEntities(15, 5, 15, null);
            for(LivingEntity ent : ents){
                if(Hostiles.contains(ent)){
                    ((Creature)ent).setTarget(p);
                }
            }
        }
    }
}
