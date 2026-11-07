package net.cree_per_.enchanting_rework.screen;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.screen.custom.NewEnchantmentScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    public static final MenuType<NewEnchantmentScreenHandler> NEW_ENCHANTMENT_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, "new_enchantment_screen_handler"),
                    new ExtendedScreenHandlerType<>(NewEnchantmentScreenHandler::new, BlockPos.STREAM_CODEC));
}
