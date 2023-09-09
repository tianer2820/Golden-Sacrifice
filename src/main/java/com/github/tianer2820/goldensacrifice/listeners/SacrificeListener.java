package com.github.tianer2820.goldensacrifice.listeners;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.github.tianer2820.goldensacrifice.utils.SacrificeHelpers;


public class SacrificeListener implements Listener {


    /**
     * place a player head on ground when player dies
     */
    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event){
        Location deathLocation = event.getPlayer().getLocation();
        Block block = deathLocation.getBlock();
        block.setType(Material.PLAYER_HEAD);

        Skull skull = (Skull)block.getState();
        skull.setOwningPlayer(event.getPlayer());

        skull.update();
    }

    /**
     * When player click the placed head, start the sacrifice
     */
    @EventHandler(ignoreCancelled = false)
    public void onClickHead(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        if(event.getHand() != EquipmentSlot.HAND){
            return;
        }
        Block block = event.getClickedBlock();
        if(block == null){
            return;
        }
        if(block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD){
            return;
        }
        if(event.getItem() != null){
            return;
        }
        SacrificeHelpers.tryBeginSacrifice(block);
    }

    /**
     * Prevent player from breaking the ongoing altar
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        if(SacrificeHelpers.isProtectedBlock(block)){
            event.setCancelled(true);
            block.getLocation().createExplosion(4f, false, false);
        }
    }
}

