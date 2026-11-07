package net.cree_per_.enchanting_rework.block.entity.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.cree_per_.enchanting_rework.block.entity.custom.NewEnchantingTableBlockEntity;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class NewEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<NewEnchantingTableBlockEntity, EnchantTableRenderState> {
    public static final Material BOOK_TEXTURE = Sheets.BLOCK_ENTITIES_MAPPER.defaultNamespaceApply("enchanting_table_book");
    private final MaterialSet spriteHolder;
    private final BookModel book;

    public NewEnchantingTableBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.spriteHolder = ctx.materials();
        this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
    }

    public EnchantTableRenderState createRenderState() {
        return new EnchantTableRenderState();
    }

    public void extractRenderState(
            NewEnchantingTableBlockEntity newEnchantingTableBlockEntity,
            EnchantTableRenderState state,
            float f,
            Vec3 vec3d,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand
    ) {
        BlockEntityRenderer.super.extractRenderState(newEnchantingTableBlockEntity, state, f, vec3d, crumblingOverlayCommand);
        state.flip = Mth.lerp(f, newEnchantingTableBlockEntity.pageAngle, newEnchantingTableBlockEntity.nextPageAngle);
        state.open = Mth.lerp(
                f, newEnchantingTableBlockEntity.pageTurningSpeed, newEnchantingTableBlockEntity.nextPageTurningSpeed
        );
        state.time = newEnchantingTableBlockEntity.ticks + f;
        float g = newEnchantingTableBlockEntity.bookRotation - newEnchantingTableBlockEntity.lastBookRotation;

        while (g >= (float) Math.PI) {
            g -= (float) (Math.PI * 2);
        }

        while (g < (float) -Math.PI) {
            g += (float) (Math.PI * 2);
        }

        state.yRot = newEnchantingTableBlockEntity.lastBookRotation + g * f;
    }

    public void submit(
            EnchantTableRenderState state,
            PoseStack matrixStack,
            SubmitNodeCollector orderedRenderCommandQueue,
            CameraRenderState cameraRenderState
    ) {
        matrixStack.pushPose();
        matrixStack.translate(0.5F, 0.75F, 0.5F);
        matrixStack.translate(0.0F, 0.1F + Mth.sin(state.time * 0.1F) * 0.01F, 0.0F);
        float f = state.yRot;
        matrixStack.mulPose(Axis.YP.rotation(-f));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(80.0F));
        float g = Mth.frac(state.flip + 0.25F) * 1.6F - 0.3F;
        float h = Mth.frac(state.flip + 0.75F) * 1.6F - 0.3F;
        BookModel.State bookModelState = new BookModel.State(
                state.time,
                Mth.clamp(g, 0.0F, 1.0F),
                Mth.clamp(h, 0.0F, 1.0F),
                state.open
        );
        orderedRenderCommandQueue.submitModel(
                this.book,
                bookModelState,
                matrixStack,
                BOOK_TEXTURE.renderType(RenderTypes::entitySolid),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                this.spriteHolder.get(BOOK_TEXTURE),
                0,
                state.breakProgress
        );
        matrixStack.popPose();
    }
}