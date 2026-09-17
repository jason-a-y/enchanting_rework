package net.cree_per_.enchanting_rework.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {
    @Inject(method = "getNextCost", at = @At("HEAD"), cancellable = true)
    private static void removeXpScaling(int cost, CallbackInfoReturnable<Integer> info){
        info.setReturnValue(cost);
    }

    /*@Definition(id = "q", local = @Local(type = int.class, ordinal = 10))
    @Definition(id = "r", local = @Local(type = int.class, ordinal = 11))
    @Expression("q == r")
    @ModifyExpressionValue(method = "updateResult", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean disableCombiningEnchantments(boolean original){
        return false;
    }*/

    @ModifyArgs(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/component/type/ItemEnchantmentsComponent$Builder;set(Lnet/minecraft/registry/entry/RegistryEntry;I)V"))
    private void disableCombiningEnchantments(Args args,
                                              @Local ItemEnchantmentsComponent.Builder builder,
                                              @Local Object2IntMap.Entry<RegistryEntry<Enchantment>> entry,
                                              @Local RegistryEntry<Enchantment> registryEntry){
        int q = builder.getLevel(registryEntry);
        int r = entry.getIntValue();
        args.set(1, Math.max(r, q));
    }
}