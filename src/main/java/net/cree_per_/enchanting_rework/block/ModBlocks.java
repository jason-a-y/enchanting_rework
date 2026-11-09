package net.cree_per_.enchanting_rework.block;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.block.custom.NewEnchantingTableBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import java.util.function.Function;

public class ModBlocks {
    public static final Block NEW_ENCHANTING_TABLE = registerBlock("new_enchanting_table",
            properties -> new NewEnchantingTableBlock(
                    properties.mapColor(MapColor.COLOR_RED)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> 7)
                            .strength(5.0F, 1200.0F)
            ));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function){
        Block toRegister = function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, name))));
        registerBlockItem(name, toRegister);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(
                EnchantingRework.MOD_ID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block){
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, name),
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, name)))));
    }

    public static void registerModBlocks(){
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(ModBlocks.NEW_ENCHANTING_TABLE);
        });
    }

    public static ResourceKey<Item> getItemResourceKey(Item item){
        return BuiltInRegistries.ITEM.getResourceKey(item).get();
    }

    public static ResourceKey<Block> getBlockResourceKey(Block block){
        return BuiltInRegistries.BLOCK.getResourceKey(block).get();
    }
}
