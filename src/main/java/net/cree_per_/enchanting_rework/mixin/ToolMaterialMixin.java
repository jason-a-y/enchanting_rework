package net.cree_per_.enchanting_rework.mixin;

import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ToolMaterial.class)
public abstract class ToolMaterialMixin {
    @Shadow
    @Final
    public static final ToolMaterial GOLD;

    @Shadow
    @Final
    public static final ToolMaterial NETHERITE;

    static {
        GOLD = new ToolMaterial(BlockTags.INCORRECT_FOR_GOLD_TOOL, 212, 12.0F, 2.0F, 22, ItemTags.GOLD_TOOL_MATERIALS);
        NETHERITE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4062, 10.0F, 4.0F, 15, ItemTags.NETHERITE_TOOL_MATERIALS);
    }
}
