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

public class UndeadPotionSplash {
        public static @Nonnull ItemStack getItemStack(int count){
        ItemStack stack = new ItemStack(Material.SPLASH_POTION, count);

        ItemMeta meta = stack.getItemMeta();
        meta.displayName(TextHelpers.italicText("\"Undead Potion\"", NamedTextColor.RED));
        meta.lore(ImmutableList.of(TextHelpers.italicText("GRRRRRRRRRRRRRRRRRR", NamedTextColor.GREEN)));
        
        meta.getPersistentDataContainer().set(CommonConstants.ITEM_ID_KEY, PersistentDataType.STRING, CommonConstants.UNDEAD_POTION_SPLASH);
        
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean isItem(ItemStack stack){
        if(stack.getType() != Material.SPLASH_POTION){
            return false;
        }
        return CommonConstants.UNDEAD_POTION_SPLASH.equals(
                stack.getItemMeta().getPersistentDataContainer().getOrDefault(CommonConstants.ITEM_ID_KEY, PersistentDataType.STRING, ""));
    }
}
