package net.cree_per_.enchanting_rework.recipe;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModRecipes {
    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_SERIALIZER = Registry.register(
            Registries.RECIPE_SERIALIZER, Identifier.of(EnchantingRework.MOD_ID, "enchantment"),
            new EnchantmentRecipe.Serializer());
    public static final RecipeType<EnchantmentRecipe> ENCHANTMENT_TYPE = Registry.register(
            Registries.RECIPE_TYPE, Identifier.of(EnchantingRework.MOD_ID, "enchantment"), new RecipeType<EnchantmentRecipe>() {
                @Override
                public String toString() {
                    return "enchantment";
                }
            });

    // I genuinely have no idea why, but the mod completely breaks without this
    // please someone explain this to me
    public static void registerRecipes() {
        EnchantingRework.LOGGER.info("Registering Custom Recipes for " + EnchantingRework.MOD_ID);
    }
}