package net.cree_per_.enchanting_rework.screen;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.screen.custom.NewEnchantmentMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    public static final MenuType<NewEnchantmentMenu> NEW_ENCHANTMENT_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, "new_enchantment_screen_handler"),
                    new ExtendedMenuType<>(NewEnchantmentMenu::new, BlockPos.STREAM_CODEC));
}
