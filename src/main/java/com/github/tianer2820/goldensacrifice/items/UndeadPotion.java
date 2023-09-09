package com.github.tianer2820.goldensacrifice.items;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import com.github.tianer2820.goldensacrifice.constants.CommonConstants;
import com.github.tianer2820.goldensacrifice.utils.TextHelpers;
import com.google.common.collect.ImmutableList;

import net.kyori.adventure.text.format.NamedTextColor;

public class UndeadPotion {
        public static @Nonnull ItemStack getItemStack(int count){
        ItemStack stack = new ItemStack(Material.POTION, count);

        ItemMeta meta = stack.getItemMeta();
        meta.displayName(TextHelpers.italicText("\"Undead Potion\"", NamedTextColor.RED));
        meta.lore(ImmutableList.of(TextHelpers.italicText("WAIT I THINK ITS BETTER NOT TO DR------", NamedTextColor.GREEN)));
        
        meta.getPersistentDataContainer().set(CommonConstants.ITEM_ID_KEY, PersistentDataType.STRING, CommonConstants.UNDEAD_POTION);
        
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean isItem(ItemStack stack){
        if(stack.getType() != Material.POTION){
            return false;
        }
        return CommonConstants.UNDEAD_POTION.equals(
                stack.getItemMeta().getPersistentDataContainer().get(CommonConstants.ITEM_ID_KEY, PersistentDataType.STRING));
    }
}
