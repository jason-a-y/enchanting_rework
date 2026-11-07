package net.cree_per_.enchanting_rework.mixin;

import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {
    @Unique
    private ContainerLevelAccess context;

    @Final
    @Shadow
    private Container resultSlots;

    @Final
    @Shadow
    Container repairSlots;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void getContext(int syncId, Inventory playerInventory, ContainerLevelAccess screenHandlerContext, CallbackInfo ci){
        context = screenHandlerContext;
    }

    @ModifyArg(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/GrindstoneMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;", ordinal = 2))
    private Slot overrideOutputSlot(Slot par1){
        return new Slot(resultSlots, 2, 129, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                context.execute((world, pos) -> {
                    int lapisCount = 0;

                    lapisCount += recipeLapisRefund(repairSlots.getItem(0));
                    lapisCount += recipeLapisRefund(repairSlots.getItem(1));

                    if (world instanceof ServerLevel) {
                        world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.LAPIS_LAZULI, lapisCount)));
                    }

                    world.levelEvent(LevelEvent.SOUND_GRINDSTONE_USED, pos, 0);
                });
                repairSlots.setItem(0, ItemStack.EMPTY);
                repairSlots.setItem(1, ItemStack.EMPTY);
            }
        };
    }

    @Unique
    private int recipeLapisRefund(ItemStack stack){
        int lapisCount = 0;
        if (!stack.isEmpty() && (stack.isEnchanted() || stack.is(Items.ENCHANTED_BOOK))){
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
            for (Holder<Enchantment> enchantment : builder.keySet()){
                if (enchantment.is(EnchantmentTags.CURSE))
                    continue;

                String enchantmentId = enchantment.getRegisteredName();

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
            if (r.output().getRegisteredName().equals(enchantmentId)){
                recipe = r;
                break;
            }
        }

        return recipe;
    }

    // check the enchantment level of an enchantment on an item, only use this after a hasEnchantment check
    @Unique
    private static int getEnchantmentLevel(ItemStack item, Holder<Enchantment> enchantment){
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(item));

        for (Holder<Enchantment> entry : builder.keySet()){
            if (entry.getRegisteredName().equals(enchantment.getRegisteredName())){
                return builder.getLevel(entry);
            }
        }

        return 0;
    }
}