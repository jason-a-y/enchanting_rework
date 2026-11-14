package net.cree_per_.enchanting_rework.datagen;

import net.cree_per_.enchanting_rework.datagen.recipe.EnchantmentRecipeBuilder;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.TOOLS, Items.TRIDENT)
                        .define('H', Items.HEART_OF_THE_SEA)
                        .define('S', Items.PRISMARINE_SHARD)
                        .define('C', Items.PRISMARINE_CRYSTALS)
                        .pattern(" CC")
                        .pattern(" HC")
                        .pattern("S  ")
                        .unlockedBy("has_heart_of_the_sea", this.has(Items.HEART_OF_THE_SEA))
                        .save(this.output);

                oreSmelting(List.of(Items.ROTTEN_FLESH), RecipeCategory.MISC, CookingBookCategory.MISC, Items.LEATHER, 0.35f, 200, "rotten_flesh");
                oreSmelting(List.of(Items.GILDED_BLACKSTONE), RecipeCategory.MISC, CookingBookCategory.BLOCKS, Items.GOLD_INGOT, 1.0f, 200, "gold_ingot");
                oreBlasting(List.of(Items.GILDED_BLACKSTONE), RecipeCategory.MISC, CookingBookCategory.BLOCKS, Items.GOLD_INGOT, 1.0f, 100, "gold_ingot");

                HolderLookup.RegistryLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);

                build(lookup, Items.PRISMARINE_CRYSTALS, List.of(2), List.of(1), Enchantments.AQUA_AFFINITY, List.of(1));
                build(lookup, Items.STICK, List.of(1,1,2,2,3), List.of(1,1,2,2,3), Enchantments.BANE_OF_ARTHROPODS, List.of(0,2,4,6,8));
                build(lookup, Items.HONEY_BLOCK, List.of(5), List.of(1), Enchantments.BINDING_CURSE, List.of(15));
                build(lookup, Items.GUNPOWDER, List.of(1,1,2,2), List.of(1,1,2,2), Enchantments.BLAST_PROTECTION, List.of(0,3,6,9));
                build(lookup, Items.GOLD_BLOCK, List.of(2,4,6,8), List.of(1,1,2,2), Enchantments.BREACH, List.of(6,9,12,15));
                build(lookup, Items.HEART_OF_THE_SEA, List.of(9), List.of(1), Enchantments.CHANNELING, List.of(12));
                build(lookup, Items.IRON_BLOCK, List.of(2,3,4,5,6), List.of(1,1,1,2,2), Enchantments.DENSITY, List.of(5,8,11,13,15));
                build(lookup, Items.NAUTILUS_SHELL, List.of(2,3,4), List.of(1,2,3), Enchantments.DEPTH_STRIDER, List.of(4,7,10));
                build(lookup, Items.GOLD_INGOT, List.of(1,2,2,3,3), List.of(2,3,4,5,7), Enchantments.EFFICIENCY, List.of(0,2,4,7,10));
                build(lookup, Items.PHANTOM_MEMBRANE, List.of(2,3,4,5), List.of(1,1,2,2), Enchantments.FEATHER_FALLING, List.of(1,3,6,9));
                build(lookup, Items.BLAZE_POWDER, List.of(3,6), List.of(1,1), Enchantments.FIRE_ASPECT, List.of(8,12));
                build(lookup, Items.COPPER_INGOT, List.of(1,1,2,2), List.of(1,1,2,2), Enchantments.FIRE_PROTECTION, List.of(0,3,6,9));
                build(lookup, Items.BLAZE_ROD, List.of(8), List.of(1), Enchantments.FLAME, List.of(10));
                build(lookup, Items.DIAMOND, List.of(3,5,7), List.of(2,3,4), Enchantments.FORTUNE, List.of(9,12,15));
                build(lookup, Items.BLUE_ICE, List.of(4,7), List.of(3,4), Enchantments.FROST_WALKER, List.of(6,12));
                build(lookup, Items.PRISMARINE_SHARD, List.of(1,2,2,3,3), List.of(2,2,2,3,3), Enchantments.IMPALING, List.of(0,2,4,6,8));
                build(lookup, Items.ECHO_SHARD, List.of(16), List.of(1), Enchantments.INFINITY, List.of(15));
                build(lookup, Items.SNOWBALL, List.of(1,2), List.of(1,2), Enchantments.KNOCKBACK, List.of(2,5));
                build(lookup, Items.ENDER_PEARL, List.of(3,5,7), List.of(2,3,4), Enchantments.LOOTING, List.of(9,12,15));
                build(lookup, Items.ENDER_EYE, List.of(3,5,7), List.of(1,1,1), Enchantments.LOYALTY, List.of(9,12,15));
                build(lookup, Items.TRIPWIRE_HOOK, List.of(1,2,3), List.of(1,1,2), Enchantments.LUCK_OF_THE_SEA, List.of(1,4,7));
                build(lookup, Items.SUGAR, List.of(3,5,7), List.of(1,2,3), Enchantments.LUNGE, List.of(8,12,15));
                build(lookup, Items.DRIED_KELP, List.of(1,1,2,2,3), List.of(1,1,2,2,3), Enchantments.LURE, List.of(0,2,4,6,8));
                build(lookup, Items.TOTEM_OF_UNDYING, List.of(24), List.of(1), Enchantments.MENDING, List.of(15));
                build(lookup, Items.ARROW, List.of(12), List.of(3), Enchantments.MULTISHOT, List.of(11));
                build(lookup, Items.POINTED_DRIPSTONE, List.of(1,2,2,3), List.of(1,1,2,3), Enchantments.PIERCING, List.of(0,3,6,9));
                build(lookup, Items.FEATHER, List.of(1,2,2,3,3), List.of(2,3,4,5,6), Enchantments.POWER, List.of(0,2,4,7,10));
                build(lookup, Items.FLINT, List.of(1,1,2,2), List.of(1,1,2,2), Enchantments.PROJECTILE_PROTECTION, List.of(0,3,6,9));
                build(lookup, Items.IRON_INGOT, List.of(1,2,2,3), List.of(1,2,2,3), Enchantments.PROTECTION, List.of(1,4,8,12));
                build(lookup, Items.STRING, List.of(1,2), List.of(1,2), Enchantments.PUNCH, List.of(2,5));
                build(lookup, Items.WIND_CHARGE, List.of(3,5,7), List.of(3,5,7), Enchantments.QUICK_CHARGE, List.of(7,12,15));
                build(lookup, Items.TURTLE_SCUTE, List.of(4,6,9), List.of(1,2,2), Enchantments.RESPIRATION, List.of(7,11,14));
                build(lookup, Items.BREEZE_ROD, List.of(4,6,8), List.of(2,2,3), Enchantments.RIPTIDE, List.of(10,13,15));
                build(lookup, Items.GLOWSTONE_DUST, List.of(1,2,2,3,3), List.of(1,2,2,3,3), Enchantments.SHARPNESS, List.of(0,2,4,7,10));
                build(lookup, Items.COBWEB, List.of(5), List.of(1), Enchantments.SILK_TOUCH, List.of(5));
                build(lookup, Items.ROTTEN_FLESH, List.of(1,1,2,2,3), List.of(1,1,2,2,3), Enchantments.SMITE, List.of(0,2,4,6,8));
                build(lookup, Items.GHAST_TEAR, List.of(2,3,4), List.of(1,1,1), Enchantments.SOUL_SPEED, List.of(6,9,12));
                build(lookup, Items.FIRE_CHARGE, List.of(3,4,5), List.of(2,3,4), Enchantments.SWEEPING_EDGE, List.of(5,8,12));
                build(lookup, Items.SCULK_SHRIEKER, List.of(5,8,11), List.of(1,1,2), Enchantments.SWIFT_SNEAK, List.of(12,14,16));
                build(lookup, Items.AMETHYST_SHARD, List.of(4,5,6), List.of(1,1,2), Enchantments.THORNS, List.of(8,12,15));
                build(lookup, Items.OBSIDIAN, List.of(1,1,1), List.of(1,1,1), Enchantments.UNBREAKING, List.of(0,3,6));
                build(lookup, Items.FERMENTED_SPIDER_EYE, List.of(6), List.of(1), Enchantments.VANISHING_CURSE, List.of(10));
                build(lookup, Items.END_CRYSTAL, List.of(6,9,12), List.of(1,2,2), Enchantments.WIND_BURST, List.of(10,13,15));
            }

            private void build(HolderLookup.RegistryLookup<Enchantment> lookup,
                               Item item,
                               List<Integer> lapis,
                               List<Integer> ingredientCost,
                               ResourceKey<Enchantment> enchantment,
                               List<Integer> bookshelves){
                EnchantmentRecipeBuilder.enchantmentRecipe(RecipeCategory.MISC, item, lapis, ingredientCost, lookup.getOrThrow(enchantment), bookshelves)
                        .unlockedBy(getHasName(item), has(item))
                        .save(this.output, "enchanting_rework:" + enchantment.identifier().getPath());
            }
        };
    }

    @Override
    public String getName() {
        return "Enchanting Rework Recipes";
    }
}