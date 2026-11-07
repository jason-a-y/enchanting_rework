package net.cree_per_.enchanting_rework.recipe;

import net.cree_per_.enchanting_rework.EnchantingRework;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipes {
    public static final RecipeSerializer<EnchantmentRecipe> ENCHANTMENT_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, "enchantment"),
            new EnchantmentRecipe.Serializer());
    public static final RecipeType<EnchantmentRecipe> ENCHANTMENT_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID, "enchantment"), new RecipeType<EnchantmentRecipe>() {
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