package net.cree_per_.enchanting_rework.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.cree_per_.enchanting_rework.block.ModBlocks;
import net.minecraft.block.Blocks;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
        return new RecipeGenerator(wrapperLookup, recipeExporter) {
            @Override
            public void generate() {
                createShaped(RecipeCategory.DECORATIONS, ModBlocks.NEW_ENCHANTING_TABLE)
                        .input('B', Items.BOOK)
                        .input('#', Blocks.OBSIDIAN)
                        .input('D', Items.DIAMOND)
                        .pattern(" B ")
                        .pattern("D#D")
                        .pattern("###")
                        .criterion("has_obsidian", this.conditionsFromItem(Blocks.OBSIDIAN))
                        .offerTo(this.exporter);

                createShaped(RecipeCategory.TOOLS, Items.TRIDENT)
                        .input('H', Items.HEART_OF_THE_SEA)
                        .input('S', Items.PRISMARINE_SHARD)
                        .input('C', Items.PRISMARINE_CRYSTALS)
                        .pattern(" CC")
                        .pattern(" HC")
                        .pattern("S  ")
                        .criterion("has_heart_of_the_sea", this.conditionsFromItem(Items.HEART_OF_THE_SEA))
                        .offerTo(this.exporter);

                offerSmelting(List.of(Items.ROTTEN_FLESH), RecipeCategory.MISC, Items.LEATHER, 0.35f, 200, "rotten_flesh");
                offerSmelting(List.of(Items.GILDED_BLACKSTONE), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0f, 200, "gold_ingot");
                offerBlasting(List.of(Items.GILDED_BLACKSTONE), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0f, 100, "gold_ingot");
            }
        };
    }

    @Override
    public String getName() {
        return "Enchanting Rework Recipes";
    }
}