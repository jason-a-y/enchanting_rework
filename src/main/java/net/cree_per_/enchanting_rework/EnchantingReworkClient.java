package net.cree_per_.enchanting_rework;

import net.cree_per_.enchanting_rework.block.entity.ModBlockEntities;
import net.cree_per_.enchanting_rework.block.entity.renderer.NewEnchantingTableBlockEntityRenderer;
import net.cree_per_.enchanting_rework.screen.ModScreenHandlers;
import net.cree_per_.enchanting_rework.screen.custom.NewEnchantmentScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class EnchantingReworkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, NewEnchantingTableBlockEntityRenderer::new);
        MenuScreens.register(ModScreenHandlers.NEW_ENCHANTMENT_SCREEN_HANDLER, NewEnchantmentScreen::new);
    }
}
