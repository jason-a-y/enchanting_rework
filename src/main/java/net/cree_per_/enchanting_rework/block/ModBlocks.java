package net.cree_per_.enchanting_rework.block;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.block.custom.NewEnchantingTableBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModBlocks {
    public static final Block NEW_ENCHANTING_TABLE = registerBlock("new_enchanting_table",
            properties -> new NewEnchantingTableBlock(
                    properties.mapColor(MapColor.RED)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresTool()
                            .luminance(state -> 7)
                            .strength(5.0F, 1200.0F)
            ));

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> function){
        Block toRegister = function.apply(AbstractBlock.Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(EnchantingRework.MOD_ID, name))));
        registerBlockItem(name, toRegister);
        return Registry.register(Registries.BLOCK, Identifier.of(
                EnchantingRework.MOD_ID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block){
        Registry.register(Registries.ITEM, Identifier.of(EnchantingRework.MOD_ID, name),
                new BlockItem(block, new Item.Settings().useBlockPrefixedTranslationKey()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(EnchantingRework.MOD_ID, name)))));
    }

    public static void registerModBlocks(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(ModBlocks.NEW_ENCHANTING_TABLE);
        });
    }
}
