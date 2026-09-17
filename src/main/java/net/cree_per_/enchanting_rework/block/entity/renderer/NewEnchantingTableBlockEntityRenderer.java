package net.cree_per_.enchanting_rework.block.entity.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.cree_per_.enchanting_rework.block.entity.custom.NewEnchantingTableBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.EnchantingTableBlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteHolder;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class NewEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<NewEnchantingTableBlockEntity, EnchantingTableBlockEntityRenderState> {
    public static final SpriteIdentifier BOOK_TEXTURE = TexturedRenderLayers.ENTITY_SPRITE_MAPPER.mapVanilla("enchanting_table_book");
    private final SpriteHolder spriteHolder;
    private final BookModel book;

    public NewEnchantingTableBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.spriteHolder = ctx.spriteHolder();
        this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
    }

    public EnchantingTableBlockEntityRenderState createRenderState() {
        return new EnchantingTableBlockEntityRenderState();
    }

    public void updateRenderState(
            NewEnchantingTableBlockEntity newEnchantingTableBlockEntity,
            EnchantingTableBlockEntityRenderState state,
            float f,
            Vec3d vec3d,
            @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlayCommand
    ) {
        BlockEntityRenderer.super.updateRenderState(newEnchantingTableBlockEntity, state, f, vec3d, crumblingOverlayCommand);
        state.pageAngle = MathHelper.lerp(f, newEnchantingTableBlockEntity.pageAngle, newEnchantingTableBlockEntity.nextPageAngle);
        state.pageTurningSpeed = MathHelper.lerp(
                f, newEnchantingTableBlockEntity.pageTurningSpeed, newEnchantingTableBlockEntity.nextPageTurningSpeed
        );
        state.ticks = newEnchantingTableBlockEntity.ticks + f;
        float g = newEnchantingTableBlockEntity.bookRotation - newEnchantingTableBlockEntity.lastBookRotation;

        while (g >= (float) Math.PI) {
            g -= (float) (Math.PI * 2);
        }

        while (g < (float) -Math.PI) {
            g += (float) (Math.PI * 2);
        }

        state.bookRotationDegrees = newEnchantingTableBlockEntity.lastBookRotation + g * f;
    }

    public void render(
            EnchantingTableBlockEntityRenderState state,
            MatrixStack matrixStack,
            OrderedRenderCommandQueue orderedRenderCommandQueue,
            CameraRenderState cameraRenderState
    ) {
        matrixStack.push();
        matrixStack.translate(0.5F, 0.75F, 0.5F);
        matrixStack.translate(0.0F, 0.1F + MathHelper.sin(state.ticks * 0.1F) * 0.01F, 0.0F);
        float f = state.bookRotationDegrees;
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(-f));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0F));
        float g = MathHelper.fractionalPart(state.pageAngle + 0.25F) * 1.6F - 0.3F;
        float h = MathHelper.fractionalPart(state.pageAngle + 0.75F) * 1.6F - 0.3F;
        BookModel.BookModelState bookModelState = new BookModel.BookModelState(
                state.ticks,
                MathHelper.clamp(g, 0.0F, 1.0F),
                MathHelper.clamp(h, 0.0F, 1.0F),
                state.pageTurningSpeed
        );
        orderedRenderCommandQueue.submitModel(
                this.book,
                bookModelState,
                matrixStack,
                BOOK_TEXTURE.getRenderLayer(RenderLayers::entitySolid),
                state.lightmapCoordinates,
                OverlayTexture.DEFAULT_UV,
                -1,
                this.spriteHolder.getSprite(BOOK_TEXTURE),
                0,
                state.crumblingOverlay
        );
        matrixStack.pop();
    }
}