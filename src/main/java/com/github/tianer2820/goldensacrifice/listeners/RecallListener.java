package com.github.tianer2820.goldensacrifice.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class RecallListener implements Listener{
    
    private Boolean debug = false;
    private void debuglog(Player p, String msg){
        if(debug){
            p.sendMessage(msg);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRecall(PlayerInteractEvent event){
        Player p = event.getPlayer();
        debuglog(p, event.getEventName());
        PotionEffect pe = p.getPotionEffect(PotionEffectType.HUNGER);
        if(pe == null) return;
        if(pe.getDuration() < 1000000000) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        Block blk = event.getClickedBlock();
        if(blk.getType() == Material.PLAYER_HEAD){
            return;
        }

        if(p.getInventory().containsAtLeast(new ItemStack(Material.PLAYER_HEAD), 1)){
            return;
        }
        ArrayList<Player> players = (ArrayList<Player>) blk.getLocation().getNearbyPlayers(5);
        for (Player pl : players) {
            if(pl.getGameMode() == GameMode.SPECTATOR) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta meta = (SkullMeta)head.getItemMeta();
                meta.setOwningPlayer(pl);
                head.setItemMeta(meta);
                p.getInventory().addItem(head);
                p.setHealth(3);
                Set<PotionEffect> effects = new HashSet<PotionEffect>();
                effects.add(new PotionEffect(PotionEffectType.DARKNESS, 20*20, 2));            
                effects.add(new PotionEffect(PotionEffectType.GLOWING, 20*60*2, 2));    
                effects.add(new PotionEffect(PotionEffectType.WEAKNESS, 20*60*2, 2));            
                p.addPotionEffects(effects);
                break;
            }
        }
    }
}