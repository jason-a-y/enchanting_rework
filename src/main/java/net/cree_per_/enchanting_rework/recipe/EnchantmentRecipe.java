package net.cree_per_.enchanting_rework.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;

public record EnchantmentRecipe(ItemStack inputItem, List<Integer> lapis, List<Integer> ingredientCost, Holder<Enchantment> output, List<Integer> bookshelves) implements Recipe<EnchantmentRecipeInput> {

    public static ArrayList<EnchantmentRecipe> enchantmentRecipes = new ArrayList<>();

    public EnchantmentRecipe {
        enchantmentRecipes.add(this);
    }

    private Ingredient inputIngredient(){
        return Ingredient.of(this.inputItem.getItem());
    }

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputIngredient());
        return list;
    }

    // read recipe JSON files, turn them into a new EnchantmentRecipe

    @Override
    public boolean matches(EnchantmentRecipeInput input, Level world) {
        if(world.isClientSide()){
            return false;
        }

        // this only checks one slot, it needs to check 2
        return inputIngredient().test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(EnchantmentRecipeInput input, HolderLookup.Provider registries) {
        // input is always the same as output item (different from output enchantment)
        return inputItem.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<EnchantmentRecipeInput>> getSerializer() {
        return ModRecipes.ENCHANTMENT_SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<EnchantmentRecipeInput>> getType() {
        return ModRecipes.ENCHANTMENT_TYPE;
    }

    @Override
    public PlacementInfo placementInfo() {
        // I'm pretty sure this also needs to check 2 slots
        return PlacementInfo.create(inputIngredient());
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    // json file translation code
    public static class Serializer implements RecipeSerializer<EnchantmentRecipe> {
        public static final MapCodec<EnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(

                ItemStack.CODEC.fieldOf("ingredient").forGetter(EnchantmentRecipe::inputItem),
                Codec.INT.listOf().fieldOf("lapis").forGetter(EnchantmentRecipe::lapis),
                Codec.INT.listOf().fieldOf("ingredient_cost").forGetter(EnchantmentRecipe::ingredientCost),
                Enchantment.CODEC.fieldOf("result").forGetter(EnchantmentRecipe::output),
                Codec.INT.listOf().fieldOf("bookshelves").forGetter(EnchantmentRecipe::bookshelves)

        ).apply(inst, EnchantmentRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ItemStack.STREAM_CODEC, EnchantmentRecipe::inputItem,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), EnchantmentRecipe::lapis,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), EnchantmentRecipe::ingredientCost,
                        Enchantment.STREAM_CODEC, EnchantmentRecipe::output,
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.INT), EnchantmentRecipe::bookshelves,
                        EnchantmentRecipe::new);

        @Override
        public MapCodec<EnchantmentRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EnchantmentRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}