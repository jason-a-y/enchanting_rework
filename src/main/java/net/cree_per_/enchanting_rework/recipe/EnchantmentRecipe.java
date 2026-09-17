package net.cree_per_.enchanting_rework.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public record EnchantmentRecipe(ItemStack inputItem, List<Integer> lapis, List<Integer> ingredientCost, RegistryEntry<Enchantment> output, List<Integer> bookshelves) implements Recipe<EnchantmentRecipeInput> {

    public static ArrayList<EnchantmentRecipe> enchantmentRecipes = new ArrayList<>();

    public EnchantmentRecipe {
        enchantmentRecipes.add(this);
    }

    private Ingredient inputIngredient(){
        return Ingredient.ofItem(this.inputItem.getItem());
    }

    public DefaultedList<Ingredient> getIngredients() {
        DefaultedList<Ingredient> list = DefaultedList.of();
        list.add(inputIngredient());
        return list;
    }

    // read recipe JSON files, turn them into a new EnchantmentRecipe

    @Override
    public boolean matches(EnchantmentRecipeInput input, World world) {
        if(world.isClient()){
            return false;
        }

        // this only checks one slot, it needs to check 2
        return inputIngredient().test(input.getStackInSlot(0));
    }

    @Override
    public ItemStack craft(EnchantmentRecipeInput input, RegistryWrapper.WrapperLookup registries) {
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
    public IngredientPlacement getIngredientPlacement() {
        // I'm pretty sure this also needs to check 2 slots
        return IngredientPlacement.forSingleSlot(inputIngredient());
    }

    @Override
    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    // json file translation code
    public static class Serializer implements RecipeSerializer<EnchantmentRecipe> {
        public static final MapCodec<EnchantmentRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(

                ItemStack.CODEC.fieldOf("ingredient").forGetter(EnchantmentRecipe::inputItem),
                Codec.INT.listOf().fieldOf("lapis").forGetter(EnchantmentRecipe::lapis),
                Codec.INT.listOf().fieldOf("ingredient_cost").forGetter(EnchantmentRecipe::ingredientCost),
                Enchantment.ENTRY_CODEC.fieldOf("result").forGetter(EnchantmentRecipe::output),
                Codec.INT.listOf().fieldOf("bookshelves").forGetter(EnchantmentRecipe::bookshelves)

        ).apply(inst, EnchantmentRecipe::new));

        public static final PacketCodec<RegistryByteBuf, EnchantmentRecipe> STREAM_CODEC =
                PacketCodec.tuple(
                        ItemStack.PACKET_CODEC, EnchantmentRecipe::inputItem,
                        PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), EnchantmentRecipe::lapis,
                        PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), EnchantmentRecipe::ingredientCost,
                        Enchantment.ENTRY_PACKET_CODEC, EnchantmentRecipe::output,
                        PacketCodecs.collection(ArrayList::new, PacketCodecs.INTEGER), EnchantmentRecipe::bookshelves,
                        EnchantmentRecipe::new);

        @Override
        public MapCodec<EnchantmentRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, EnchantmentRecipe> packetCodec() {
            return STREAM_CODEC;
        }
    }
}