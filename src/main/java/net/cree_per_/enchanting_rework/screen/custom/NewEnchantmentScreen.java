package net.cree_per_.enchanting_rework.screen.custom;

import com.google.common.collect.Lists;
import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentNames;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

public class NewEnchantmentScreen extends AbstractContainerScreen<NewEnchantmentScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID,"textures/gui/container/enchanting_table.png");
    private static final Identifier BOOK_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/enchanting_table_book.png");
    private static final Identifier SCROLLER_TEXTURE = Identifier.withDefaultNamespace("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.withDefaultNamespace("container/stonecutter/scroller_disabled");
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private static final int SCROLLBAR_AREA_HEIGHT = 57;
    private static final int ENCHANTMENT_SLOT_HEIGHT = 19;

    private final RandomSource random = RandomSource.create();
    private BookModel BOOK_MODEL;
    public float nextPageAngle;
    public float pageAngle;
    public float approximatePageAngle;
    public float pageRotationSpeed;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;

    private ItemStack stack = ItemStack.EMPTY;
    private boolean canEnchant;
    // used for scrolling with the mouse
    private boolean mouseClicked;
    // stores how much the user has scrolled
    private float scrollAmount;
    // used to offset the recipe icons
    private int scrollOffset;

    private static final Identifier ENCHANTMENT_SLOT_DISABLED_TEXTURE = Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_disabled");
    private static final Identifier ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE = Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot_highlighted");
    private static final Identifier ENCHANTMENT_SLOT_TEXTURE = Identifier.withDefaultNamespace("container/enchanting_table/enchantment_slot");

    private static final Identifier ENCHANTMENT_BUTTON_DISABLED_TEXTURE = Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID,"textures/gui/sprites/container/enchanting_table/button_disabled.png");
    private static final Identifier ENCHANTMENT_BUTTON_HIGHLIGHTED_TEXTURE = Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID,"textures/gui/sprites/container/enchanting_table/button_highlighted.png");
    private static final Identifier ENCHANTMENT_BUTTON_TEXTURE = Identifier.fromNamespaceAndPath(EnchantingRework.MOD_ID,"textures/gui/sprites/container/enchanting_table/button.png");

    public NewEnchantmentScreen(NewEnchantmentScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.imageWidth = 188;
        this.imageHeight = 178;
        this.inventoryLabelY = this.imageHeight - 94;

        handler.setContentsChangedListener(this::onInventoryChange);
    }

    @Override
    protected void init() {
        super.init();
        this.BOOK_MODEL = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.minecraft.player.experienceDisplayStartTick = this.minecraft.player.tickCount;
        this.doTick();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        this.mouseClicked = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        // enchantment button
        double startX = click.x() - (i + 34);
        double startY = click.y() - (j + 66);
        if (startX >= 0.0 && startY >= 0.0 && startX < 18.0 && startY < 18.0 && this.menu.clickMenuButton(this.minecraft.player, 0)) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            return true;
        }

        int offset = this.scrollOffset / 19;
        // enchantment selectors
        for (int k = 0; k < 3; k++) {
            double d = click.x() - (i + 60);
            double e = click.y() - (j + 14 + ENCHANTMENT_SLOT_HEIGHT * k);
            if (d >= 0.0 && e >= 0.0 && d < 108.0 && e < 19.0 && this.menu.clickMenuButton(this.minecraft.player, k + 1 + offset)) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, k + 1 + offset);
                return true;
            }
        }

        // if the mouse is over the scroll wheel
        i = this.leftPos + 171;
        j = this.topPos + 9;
        if (click.x() >= i && click.x() < i + 12 && click.y() >= j && click.y() < j + SCROLLBAR_AREA_HEIGHT) {
            this.mouseClicked = true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.topPos + 14;
            int j = i + SCROLLBAR_AREA_HEIGHT;
            this.scrollAmount = ((float)click.y() - i - 7.5F) / (j - i - 15.0F);
            this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0F, 1.0F);
            this.scrollOffset = (int)(this.scrollAmount * this.getMaxScroll() + 0.5) * ENCHANTMENT_SLOT_HEIGHT;
            return true;
        } else {
            return super.mouseDragged(click, offsetX, offsetY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        } else {
            if (this.shouldScroll()) {
                int i = this.getMaxScroll();
                float f = (float)verticalAmount / i;
                this.scrollAmount = Mth.clamp(this.scrollAmount - f, 0.0F, 1.0F);
                this.scrollOffset = (int)(this.scrollAmount * i + 0.5) * ENCHANTMENT_SLOT_HEIGHT;
            }

            return true;
        }
    }

    private boolean shouldScroll() {
        return this.canEnchant && this.menu.getValidEnchantments().size() > 3;
    }

    private int getMaxScroll() {
        return this.menu.getValidEnchantments().size() - 3;
    }

    private void onInventoryChange() {
        this.canEnchant = this.menu.canEnchant();
        if (!this.canEnchant) {
            this.scrollAmount = 0.0F;
            this.scrollOffset = 0;
        }
    }

    @Override
    protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.drawBook(context, i, j);

        // scrollbar
        int amount = (int)(41.0F * this.scrollAmount);
        Identifier identifier = this.shouldScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        context.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, i + 171, j + SCROLLBAR_HEIGHT + amount, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

        List<String> validEnchantments = this.menu.getValidEnchantments();

        // disabled slots
        for (int k = 0; k < 3; k++) {
            context.blitSprite(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_TEXTURE, i + 60, j + 14 + ENCHANTMENT_SLOT_HEIGHT * k, 108, ENCHANTMENT_SLOT_HEIGHT);
        }

        int offset = this.scrollOffset / 19;

        EnchantmentNames.getInstance().initSeed(this.menu.getSeed());
        // enabled slots, pasted on top
        for (int k = offset; k < offset + 3 && k < validEnchantments.size(); k++) {
            Identifier slotTexture;
            int p = Mth.clamp(this.font.width(validEnchantments.get(k)), 0, 72);
            FormattedText stringVisitable = EnchantmentNames.getInstance().getRandomName(this.font, p);
            int q;

            // if I can afford an enchantment, enable the slot, else, disable it
            if (this.menu.canAfford(validEnchantments.get(k))){
                slotTexture = ENCHANTMENT_SLOT_TEXTURE;
                q = 0x685e4a;

                // highlight the slot that is being hovered over
                int mX = mouseX - (i + 60);
                int mY = mouseY - (j + 14 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset);
                if (mX >= 0 && mY >= 0 && mX < 108 && mY < 19) {
                    slotTexture = ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE;
                    q = 0xffff80;
                }
            } else {
                drawIngredients(context, validEnchantments.get(k), i, j, k);
                continue;
            }
            context.blitSprite(RenderPipelines.GUI_TEXTURED, slotTexture, i + 60, j + 14 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, 108, ENCHANTMENT_SLOT_HEIGHT);
            context.drawWordWrap(this.font, stringVisitable, i + 80, j + 16 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, p, ARGB.opaque(q), false);

            drawIngredients(context, validEnchantments.get(k), i, j, k);
        }

        // enchantment button
        if (validEnchantments.isEmpty()){
            context.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_BUTTON_DISABLED_TEXTURE, i + 34, j + 66,0.0F, 0.0F, 18, 18, 18, 18);
        } else {
            int mX = mouseX - (i + 34);
            int mY = mouseY - (j + 66);
            if (mX >= 0 && mY >= 0 && mX < 18 && mY < 18){
                context.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_BUTTON_HIGHLIGHTED_TEXTURE, i + 34, j + 66,0.0F, 0.0F, 18, 18, 18, 18);
            } else {
                context.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_BUTTON_TEXTURE, i + 34, j + 66,0.0F, 0.0F, 18, 18, 18, 18);
            }

            String bookshelfCount = "" + this.menu.bookshelfPower();

            context.renderItem(Items.BOOKSHELF.getDefaultInstance(), i + 35, j + 67);
            context.drawString(this.font, bookshelfCount, i + 52 - bookshelfCount.length() * 6, j + 76, ARGB.opaque(0xffffff));
        }
    }

    private void drawIngredients(GuiGraphics context, String enchantmentId, int i, int j, int k){
        String ingredientCount = "" + this.menu.recipeIngredientCost(enchantmentId);
        String lapisCount = "" + this.menu.recipeLapisCost(enchantmentId);

        if (ingredientCount.equals("1")){
            ingredientCount = "";
        }
        if (lapisCount.equals("1")){
            lapisCount = "";
        }
        EnchantmentRecipe recipe = NewEnchantmentScreenHandler.recipeFromEnchantmentId(enchantmentId);

        ItemStack displayItem = recipe == null ? ItemStack.EMPTY : recipe.inputItem();

        context.renderItem(displayItem, i + 61, j + 15 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset);
        context.drawString(this.font, ingredientCount, i + 78 - ingredientCount.length() * 6, j + 24 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, ARGB.opaque(0xffffff));

        if (!lapisCount.equals("0")){
            context.renderItem(Items.LAPIS_LAZULI.getDefaultInstance(), i + 150, j + 15 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset);
            context.drawString(this.font, lapisCount, i + 167 - lapisCount.length() * 6, j + 24 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, ARGB.opaque(0xffffff));
        }
    }

    private void drawBook(GuiGraphics context, int x, int y) {
        float f = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float g = Mth.lerp(f, this.pageTurningSpeed, this.nextPageTurningSpeed);
        float h = Mth.lerp(f, this.pageAngle, this.nextPageAngle);
        int i = x + 14;
        int j = y + 14;
        int k = i + 38;
        int l = j + 31;
        context.submitBookModelRenderState(this.BOOK_MODEL, BOOK_TEXTURE, 40.0F, g, h, i, j, k, l);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        float f = this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        super.render(context, mouseX, mouseY, f);
        this.renderTooltip(context, mouseX, mouseY);

        List<String> validEnchantments = this.menu.getValidEnchantments();

        int offset = this.scrollOffset / 19;

        for (int i = offset; i < offset + 3 && i < validEnchantments.size(); i++) {
            List<Component> list = Lists.newArrayList();

            int enchantmentLevel = this.menu.getCurrentToolEnchantmentLevel(validEnchantments.get(i)) + 1;

            // index 0 = namespace, index 1 = identifier
            String[] splitIdentifier = validEnchantments.get(i).split(":");
            String translateText = "enchantment." + splitIdentifier[0] + "." + splitIdentifier[1];

            if (enchantmentLevel == 1){
                list.add(Component.translatable(translateText));
            } else {
                list.add(Component.translatable(translateText).append(" ").append(Component.translatable("enchantment.level." + enchantmentLevel)));
            }

            if (this.isHovering(60, 14 + ENCHANTMENT_SLOT_HEIGHT * i - this.scrollOffset, 108, 17, mouseX, mouseY) /*&& this.handler.canAfford(validEnchantments.get(i))*/) {
                context.setComponentTooltipForNextFrame(this.font, list, mouseX, mouseY);
                break;
            }
        }
    }

    public void doTick() {
        ItemStack itemStack = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(itemStack, this.stack)) {
            this.stack = itemStack;

            do {
                this.approximatePageAngle = this.approximatePageAngle + (this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.nextPageAngle <= this.approximatePageAngle + 1.0F && this.nextPageAngle >= this.approximatePageAngle - 1.0F);
        }

        this.pageAngle = this.nextPageAngle;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        boolean isTableActive = false;

        for (int i = 0; i < 3; i++) {
            if (!this.menu.getValidEnchantments().isEmpty()) {
                isTableActive = true;
            }
        }

        if (isTableActive) {
            this.nextPageTurningSpeed += 0.2F;
        } else {
            this.nextPageTurningSpeed -= 0.2F;
        }

        this.nextPageTurningSpeed = Mth.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
        float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4F;
        float g = 0.2F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        this.pageRotationSpeed = this.pageRotationSpeed + (f - this.pageRotationSpeed) * 0.9F;
        this.nextPageAngle = this.nextPageAngle + this.pageRotationSpeed;
    }
}