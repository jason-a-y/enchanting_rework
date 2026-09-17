package net.cree_per_.enchanting_rework.screen.custom;

import com.google.common.collect.Lists;
import net.cree_per_.enchanting_rework.EnchantingRework;
import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.util.List;

public class NewEnchantmentScreen extends HandledScreen<NewEnchantmentScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.of(EnchantingRework.MOD_ID,"textures/gui/container/enchanting_table.png");
    private static final Identifier BOOK_TEXTURE =
            Identifier.ofVanilla("textures/entity/enchanting_table_book.png");
    private static final Identifier SCROLLER_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller");
    private static final Identifier SCROLLER_DISABLED_TEXTURE = Identifier.ofVanilla("container/stonecutter/scroller_disabled");
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private static final int SCROLLBAR_AREA_HEIGHT = 57;
    private static final int ENCHANTMENT_SLOT_HEIGHT = 19;

    private final Random random = Random.create();
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

    private static final Identifier ENCHANTMENT_SLOT_DISABLED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_disabled");
    private static final Identifier ENCHANTMENT_SLOT_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot_highlighted");
    private static final Identifier ENCHANTMENT_SLOT_TEXTURE = Identifier.ofVanilla("container/enchanting_table/enchantment_slot");

    private static final Identifier ENCHANTMENT_BUTTON_DISABLED_TEXTURE = Identifier.of(EnchantingRework.MOD_ID,"textures/gui/sprites/container/enchanting_table/button_disabled.png");
    private static final Identifier ENCHANTMENT_BUTTON_HIGHLIGHTED_TEXTURE = Identifier.of(EnchantingRework.MOD_ID,"textures/gui/sprites/container/enchanting_table/button_highlighted.png");
    private static final Identifier ENCHANTMENT_BUTTON_TEXTURE = Identifier.of(EnchantingRework.MOD_ID,"textures/gui/sprites/container/enchanting_table/button.png");

    public NewEnchantmentScreen(NewEnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 188;
        this.backgroundHeight = 178;
        this.playerInventoryTitleY = this.backgroundHeight - 94;

        handler.setContentsChangedListener(this::onInventoryChange);
    }

    @Override
    protected void init() {
        super.init();
        this.BOOK_MODEL = new BookModel(this.client.getLoadedEntityModels().getModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.client.player.experienceBarDisplayStartTime = this.client.player.age;
        this.doTick();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        this.mouseClicked = false;
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;

        // enchantment button
        double startX = click.x() - (i + 34);
        double startY = click.y() - (j + 66);
        if (startX >= 0.0 && startY >= 0.0 && startX < 18.0 && startY < 18.0 && this.handler.onButtonClick(this.client.player, 0)) {
            this.client.interactionManager.clickButton(this.handler.syncId, 0);
            return true;
        }

        int offset = this.scrollOffset / 19;
        // enchantment selectors
        for (int k = 0; k < 3; k++) {
            double d = click.x() - (i + 60);
            double e = click.y() - (j + 14 + ENCHANTMENT_SLOT_HEIGHT * k);
            if (d >= 0.0 && e >= 0.0 && d < 108.0 && e < 19.0 && this.handler.onButtonClick(this.client.player, k + 1 + offset)) {
                this.client.interactionManager.clickButton(this.handler.syncId, k + 1 + offset);
                return true;
            }
        }

        // if the mouse is over the scroll wheel
        i = this.x + 171;
        j = this.y + 9;
        if (click.x() >= i && click.x() < i + 12 && click.y() >= j && click.y() < j + SCROLLBAR_AREA_HEIGHT) {
            this.mouseClicked = true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (this.mouseClicked && this.shouldScroll()) {
            int i = this.y + 14;
            int j = i + SCROLLBAR_AREA_HEIGHT;
            this.scrollAmount = ((float)click.y() - i - 7.5F) / (j - i - 15.0F);
            this.scrollAmount = MathHelper.clamp(this.scrollAmount, 0.0F, 1.0F);
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
                this.scrollAmount = MathHelper.clamp(this.scrollAmount - f, 0.0F, 1.0F);
                this.scrollOffset = (int)(this.scrollAmount * i + 0.5) * ENCHANTMENT_SLOT_HEIGHT;
            }

            return true;
        }
    }

    private boolean shouldScroll() {
        return this.canEnchant && this.handler.getValidEnchantments().size() > 3;
    }

    private int getMaxScroll() {
        return this.handler.getValidEnchantments().size() - 3;
    }

    private void onInventoryChange() {
        this.canEnchant = this.handler.canEnchant();
        if (!this.canEnchant) {
            this.scrollAmount = 0.0F;
            this.scrollOffset = 0;
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);
        this.drawBook(context, i, j);

        // scrollbar
        int amount = (int)(41.0F * this.scrollAmount);
        Identifier identifier = this.shouldScroll() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, i + 171, j + SCROLLBAR_HEIGHT + amount, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

        List<String> validEnchantments = this.handler.getValidEnchantments();

        // disabled slots
        for (int k = 0; k < 3; k++) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_SLOT_DISABLED_TEXTURE, i + 60, j + 14 + ENCHANTMENT_SLOT_HEIGHT * k, 108, ENCHANTMENT_SLOT_HEIGHT);
        }

        int offset = this.scrollOffset / 19;

        EnchantingPhrases.getInstance().setSeed(this.handler.getSeed());
        // enabled slots, pasted on top
        for (int k = offset; k < offset + 3 && k < validEnchantments.size(); k++) {
            Identifier slotTexture;
            int p = MathHelper.clamp(this.textRenderer.getWidth(validEnchantments.get(k)), 0, 72);
            StringVisitable stringVisitable = EnchantingPhrases.getInstance().generatePhrase(this.textRenderer, p);
            int q;

            // if I can afford an enchantment, enable the slot, else, disable it
            if (this.handler.canAfford(validEnchantments.get(k))){
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
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, slotTexture, i + 60, j + 14 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, 108, ENCHANTMENT_SLOT_HEIGHT);
            context.drawWrappedText(this.textRenderer, stringVisitable, i + 80, j + 16 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, p, ColorHelper.fullAlpha(q), false);

            drawIngredients(context, validEnchantments.get(k), i, j, k);
        }

        // enchantment button
        if (validEnchantments.isEmpty()){
            context.drawTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_BUTTON_DISABLED_TEXTURE, i + 34, j + 66,0.0F, 0.0F, 18, 18, 18, 18);
        } else {
            int mX = mouseX - (i + 34);
            int mY = mouseY - (j + 66);
            if (mX >= 0 && mY >= 0 && mX < 18 && mY < 18){
                context.drawTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_BUTTON_HIGHLIGHTED_TEXTURE, i + 34, j + 66,0.0F, 0.0F, 18, 18, 18, 18);
            } else {
                context.drawTexture(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_BUTTON_TEXTURE, i + 34, j + 66,0.0F, 0.0F, 18, 18, 18, 18);
            }

            String bookshelfCount = "" + this.handler.bookshelfPower();

            context.drawItem(Items.BOOKSHELF.getDefaultStack(), i + 35, j + 67);
            context.drawTextWithShadow(this.textRenderer, bookshelfCount, i + 52 - bookshelfCount.length() * 6, j + 76, ColorHelper.fullAlpha(0xffffff));
        }
    }

    private void drawIngredients(DrawContext context, String enchantmentId, int i, int j, int k){
        String ingredientCount = "" + this.handler.recipeIngredientCost(enchantmentId);
        String lapisCount = "" + this.handler.recipeLapisCost(enchantmentId);

        if (ingredientCount.equals("1")){
            ingredientCount = "";
        }
        if (lapisCount.equals("1")){
            lapisCount = "";
        }
        EnchantmentRecipe recipe = NewEnchantmentScreenHandler.recipeFromEnchantmentId(enchantmentId);

        ItemStack displayItem = recipe == null ? ItemStack.EMPTY : recipe.inputItem();

        context.drawItem(displayItem, i + 61, j + 15 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset);
        context.drawTextWithShadow(this.textRenderer, ingredientCount, i + 78 - ingredientCount.length() * 6, j + 24 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, ColorHelper.fullAlpha(0xffffff));

        if (!lapisCount.equals("0")){
            context.drawItem(Items.LAPIS_LAZULI.getDefaultStack(), i + 150, j + 15 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset);
            context.drawTextWithShadow(this.textRenderer, lapisCount, i + 167 - lapisCount.length() * 6, j + 24 + ENCHANTMENT_SLOT_HEIGHT * k - this.scrollOffset, ColorHelper.fullAlpha(0xffffff));
        }
    }

    private void drawBook(DrawContext context, int x, int y) {
        float f = this.client.getRenderTickCounter().getTickProgress(false);
        float g = MathHelper.lerp(f, this.pageTurningSpeed, this.nextPageTurningSpeed);
        float h = MathHelper.lerp(f, this.pageAngle, this.nextPageAngle);
        int i = x + 14;
        int j = y + 14;
        int k = i + 38;
        int l = j + 31;
        context.addBookModel(this.BOOK_MODEL, BOOK_TEXTURE, 40.0F, g, h, i, j, k, l);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        float f = this.client.getRenderTickCounter().getTickProgress(false);
        super.render(context, mouseX, mouseY, f);
        this.drawMouseoverTooltip(context, mouseX, mouseY);

        List<String> validEnchantments = this.handler.getValidEnchantments();

        int offset = this.scrollOffset / 19;

        for (int i = offset; i < offset + 3 && i < validEnchantments.size(); i++) {
            List<Text> list = Lists.newArrayList();

            int enchantmentLevel = this.handler.getCurrentToolEnchantmentLevel(validEnchantments.get(i)) + 1;

            // index 0 = namespace, index 1 = identifier
            String[] splitIdentifier = validEnchantments.get(i).split(":");
            String translateText = "enchantment." + splitIdentifier[0] + "." + splitIdentifier[1];

            if (enchantmentLevel == 1){
                list.add(Text.translatable(translateText));
            } else {
                list.add(Text.translatable(translateText).append(" ").append(Text.translatable("enchantment.level." + enchantmentLevel)));
            }

            if (this.isPointWithinBounds(60, 14 + ENCHANTMENT_SLOT_HEIGHT * i - this.scrollOffset, 108, 17, mouseX, mouseY) /*&& this.handler.canAfford(validEnchantments.get(i))*/) {
                context.drawTooltip(this.textRenderer, list, mouseX, mouseY);
                break;
            }
        }
    }

    public void doTick() {
        ItemStack itemStack = this.handler.getSlot(0).getStack();
        if (!ItemStack.areEqual(itemStack, this.stack)) {
            this.stack = itemStack;

            do {
                this.approximatePageAngle = this.approximatePageAngle + (this.random.nextInt(4) - this.random.nextInt(4));
            } while (this.nextPageAngle <= this.approximatePageAngle + 1.0F && this.nextPageAngle >= this.approximatePageAngle - 1.0F);
        }

        this.pageAngle = this.nextPageAngle;
        this.pageTurningSpeed = this.nextPageTurningSpeed;
        boolean isTableActive = false;

        for (int i = 0; i < 3; i++) {
            if (!this.handler.getValidEnchantments().isEmpty()) {
                isTableActive = true;
            }
        }

        if (isTableActive) {
            this.nextPageTurningSpeed += 0.2F;
        } else {
            this.nextPageTurningSpeed -= 0.2F;
        }

        this.nextPageTurningSpeed = MathHelper.clamp(this.nextPageTurningSpeed, 0.0F, 1.0F);
        float f = (this.approximatePageAngle - this.nextPageAngle) * 0.4F;
        float g = 0.2F;
        f = MathHelper.clamp(f, -0.2F, 0.2F);
        this.pageRotationSpeed = this.pageRotationSpeed + (f - this.pageRotationSpeed) * 0.9F;
        this.nextPageAngle = this.nextPageAngle + this.pageRotationSpeed;
    }
}