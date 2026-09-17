package net.cree_per_.enchanting_rework.block.entity;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.block.ModBlocks;
import net.cree_per_.enchanting_rework.block.entity.custom.NewEnchantingTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<NewEnchantingTableBlockEntity> NEW_ENCHANTING_TABLE_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(EnchantingRework.MOD_ID, "new_enchanting_table_block_entity"),
                    FabricBlockEntityTypeBuilder.create(NewEnchantingTableBlockEntity::new, ModBlocks.NEW_ENCHANTING_TABLE).build());
}
