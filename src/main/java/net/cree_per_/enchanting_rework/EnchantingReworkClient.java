package net.cree_per_.enchanting_rework;

import net.cree_per_.enchanting_rework.block.entity.ModBlockEntities;
import net.cree_per_.enchanting_rework.block.entity.renderer.NewEnchantingTableBlockEntityRenderer;
import net.cree_per_.enchanting_rework.screen.ModScreenHandlers;
import net.cree_per_.enchanting_rework.screen.custom.NewEnchantmentScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class EnchantingReworkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererFactories.register(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, NewEnchantingTableBlockEntityRenderer::new);
        HandledScreens.register(ModScreenHandlers.NEW_ENCHANTMENT_SCREEN_HANDLER, NewEnchantmentScreen::new);
    }
}
