package net.cree_per_.enchanting_rework.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.cree_per_.enchanting_rework.block.ModBlocks;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider wrapperLookup, RecipeOutput recipeExporter) {
        return new RecipeProvider(wrapperLookup, recipeExporter) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.DECORATIONS, ModBlocks.NEW_ENCHANTING_TABLE)
                        .define('B', Items.BOOK)
                        .define('#', Blocks.OBSIDIAN)
                        .define('D', Items.DIAMOND)
                        .pattern(" B ")
                        .pattern("D#D")
                        .pattern("###")
                        .unlockedBy("has_obsidian", this.has(Blocks.OBSIDIAN))
                        .save(this.output);

                shaped(RecipeCategory.TOOLS, Items.TRIDENT)
                        .define('H', Items.HEART_OF_THE_SEA)
                        .define('S', Items.PRISMARINE_SHARD)
                        .define('C', Items.PRISMARINE_CRYSTALS)
                        .pattern(" CC")
                        .pattern(" HC")
                        .pattern("S  ")
                        .unlockedBy("has_heart_of_the_sea", this.has(Items.HEART_OF_THE_SEA))
                        .save(this.output);

                oreSmelting(List.of(Items.ROTTEN_FLESH), RecipeCategory.MISC, Items.LEATHER, 0.35f, 200, "rotten_flesh");
                oreSmelting(List.of(Items.GILDED_BLACKSTONE), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0f, 200, "gold_ingot");
                oreBlasting(List.of(Items.GILDED_BLACKSTONE), RecipeCategory.MISC, Items.GOLD_INGOT, 1.0f, 100, "gold_ingot");
            }
        };
    }

    @Override
    public String getName() {
        return "Enchanting Rework Recipes";
    }
}