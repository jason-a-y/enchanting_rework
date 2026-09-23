package net.cree_per_.enchanting_rework.mixin;

import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin {
    @Unique
    private ScreenHandlerContext context;

    @Final
    @Shadow
    private Inventory result;

    @Final
    @Shadow
    Inventory input;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    private void getContext(int syncId, PlayerInventory playerInventory, ScreenHandlerContext screenHandlerContext, CallbackInfo ci){
        context = screenHandlerContext;
    }

    @ModifyArg(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/GrindstoneScreenHandler;addSlot(Lnet/minecraft/screen/slot/Slot;)Lnet/minecraft/screen/slot/Slot;", ordinal = 2))
    private Slot overrideOutputSlot(Slot par1){
        return new Slot(result, 2, 129, 34) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public void onTakeItem(PlayerEntity player, ItemStack stack) {
                context.run((world, pos) -> {
                    int lapisCount = 0;

                    lapisCount += recipeLapisRefund(input.getStack(0));
                    lapisCount += recipeLapisRefund(input.getStack(1));

                    if (world instanceof ServerWorld) {
                        world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.LAPIS_LAZULI, lapisCount)));
                    }

                    world.syncWorldEvent(WorldEvents.GRINDSTONE_USED, pos, 0);
                });
                input.setStack(0, ItemStack.EMPTY);
                input.setStack(1, ItemStack.EMPTY);
            }
        };
    }

    @Unique
    private int recipeLapisRefund(ItemStack stack){
        int lapisCount = 0;
        if (!stack.isEmpty() && (stack.hasEnchantments() || stack.isOf(Items.ENCHANTED_BOOK))){
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(stack));
            for (RegistryEntry<Enchantment> enchantment : builder.getEnchantments()){
                if (enchantment.isIn(EnchantmentTags.CURSE))
                    continue;

                String enchantmentId = enchantment.getIdAsString();

                EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);

                if (recipe == null) continue;

                int currentEnchantmentLevel = getEnchantmentLevel(stack, enchantment);
                // adds the lapis cost of all previous levels
                int totalCost = 0;
                for (int cost : recipe.lapis().subList(0, currentEnchantmentLevel)){
                    totalCost += cost;
                }
                // refunds half of the lapis
                lapisCount += totalCost / 2;
            }
        }
        return lapisCount;
    }

    // these 2 methods below are copied from NewEnchantmentScreenHandler.java
    @Unique
    private static EnchantmentRecipe recipeFromEnchantmentId(String enchantmentId){
        ArrayList<EnchantmentRecipe> recipes = EnchantmentRecipe.enchantmentRecipes;
        EnchantmentRecipe recipe = null;

        for (EnchantmentRecipe r : recipes){
            if (r.output().getIdAsString().equals(enchantmentId)){
                recipe = r;
                break;
            }
        }

        return recipe;
    }

    // check the enchantment level of an enchantment on an item, only use this after a hasEnchantment check
    @Unique
    private static int getEnchantmentLevel(ItemStack item, RegistryEntry<Enchantment> enchantment){
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(item));

        for (RegistryEntry<Enchantment> entry : builder.getEnchantments()){
            if (entry.getIdAsString().equals(enchantment.getIdAsString())){
                return builder.getLevel(entry);
            }
        }

        return 0;
    }
}