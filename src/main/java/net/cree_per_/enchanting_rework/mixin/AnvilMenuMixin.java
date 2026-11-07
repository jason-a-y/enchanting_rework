package net.cree_per_.enchanting_rework.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
    @Inject(method = "calculateIncreasedRepairCost", at = @At("HEAD"), cancellable = true)
    private static void removeXpScaling(int cost, CallbackInfoReturnable<Integer> info){
        info.setReturnValue(cost);
    }

    @ModifyArgs(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/ItemEnchantments$Mutable;set(Lnet/minecraft/core/Holder;I)V"))
    private void disableCombiningEnchantments(Args args,
                                              @Local ItemEnchantments.Mutable builder,
                                              @Local Object2IntMap.Entry<Holder<Enchantment>> entry,
                                              @Local Holder<Enchantment> registryEntry){
        int q = builder.getLevel(registryEntry);
        int r = entry.getIntValue();
        args.set(1, Math.max(r, q));
    }
}