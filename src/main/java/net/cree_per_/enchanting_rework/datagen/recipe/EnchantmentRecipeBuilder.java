package net.cree_per_.enchanting_rework.datagen.recipe;

import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class EnchantmentRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;

    private final ItemStackTemplate input; // might need to be ItemStackTemplate
    private final List<Integer> lapis;
    private final List<Integer> ingredientCost;
    private final Holder<Enchantment> recipeOutput;
    private final List<Integer> bookshelves;

    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    private @Nullable String group;

    private EnchantmentRecipeBuilder(RecipeCategory category,
                                     ItemStackTemplate input,
                                     List<Integer> lapis,
                                     List<Integer> ingredientCost,
                                     Holder<Enchantment> output,
                                     List<Integer> bookshelves) {
        this.category = category;
        this.input = input;
        this.lapis = lapis;
        this.ingredientCost = ingredientCost;
        this.recipeOutput = output;
        this.bookshelves = bookshelves;
    }

    public static EnchantmentRecipeBuilder enchantmentRecipe(RecipeCategory category,
                                                             Item input,
                                                             List<Integer> lapis,
                                                             List<Integer> ingredientCost,
                                                             Holder<Enchantment> output,
                                                             List<Integer> bookshelves){
        return new EnchantmentRecipeBuilder(category, new ItemStackTemplate(input), lapis, ingredientCost, output, bookshelves);
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.input);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        EnchantmentRecipe recipe = new EnchantmentRecipe(input, lapis, ingredientCost, recipeOutput, bookshelves);
        output.accept(id, recipe, this.advancementBuilder.build(output, id, this.category));
    }
}
