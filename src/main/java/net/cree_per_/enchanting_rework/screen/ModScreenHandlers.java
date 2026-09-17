package net.cree_per_.enchanting_rework.screen;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.screen.custom.NewEnchantmentScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
    public static final ScreenHandlerType<NewEnchantmentScreenHandler> NEW_ENCHANTMENT_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(EnchantingRework.MOD_ID, "new_enchantment_screen_handler"),
                    new ExtendedScreenHandlerType<>(NewEnchantmentScreenHandler::new, BlockPos.PACKET_CODEC));
}
