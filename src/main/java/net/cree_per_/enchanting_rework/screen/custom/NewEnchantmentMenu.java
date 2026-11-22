package net.cree_per_.enchanting_rework.screen.custom;

import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.cree_per_.enchanting_rework.block.custom.NewEnchantingTableBlock;
import net.cree_per_.enchanting_rework.block.entity.custom.NewEnchantingTableBlockEntity;
import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipeInput;
import net.cree_per_.enchanting_rework.recipe.ModRecipes;
import net.cree_per_.enchanting_rework.screen.ModScreenHandlers;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Util;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;

public class NewEnchantmentMenu extends AbstractContainerMenu {
    static final Identifier EMPTY_LAPIS_LAZULI_SLOT_TEXTURE = Identifier.withDefaultNamespace("container/slot/lapis_lazuli");
    private final Container inventory;
    public final NewEnchantingTableBlockEntity blockEntity;

    public static final List<String> listOfEnchantments = listOfEnchantments();

    private final DataSlot seed = DataSlot.standalone();

    private static final int TOOL_SLOT = 0;
    private static final int INGREDIENT_SLOT = 1;
    private static final int LAPIS_SLOT = 2;

    Runnable contentsChangedListener = () -> {};

    public NewEnchantmentMenu(int syncId, Inventory inventory, BlockPos pos) {
        this(syncId, inventory, inventory.player.level().getBlockEntity(pos));
    }

    public NewEnchantmentMenu(int syncId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModScreenHandlers.NEW_ENCHANTMENT_SCREEN_HANDLER, syncId);
        this.inventory = ((Container) blockEntity);
        this.blockEntity = ((NewEnchantingTableBlockEntity) blockEntity);

        // tool slot
        this.addSlot(new Slot(inventory, 0, 15, 47){
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                NewEnchantmentMenu.this.contentsChangedListener.run();
            }
        });
        // ingredient slot
        this.addSlot(new Slot(inventory, 1, 35, 47){
            @Override
            public void setChanged() {
                super.setChanged();
                NewEnchantmentMenu.this.contentsChangedListener.run();
            }
        });
        // lapis slot
        this.addSlot(new Slot(inventory, 2, 15, 67){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.LAPIS_LAZULI);
            }

            @Override
            public Identifier getNoItemIcon() {
                return NewEnchantmentMenu.EMPTY_LAPIS_LAZULI_SLOT_TEXTURE;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                NewEnchantmentMenu.this.contentsChangedListener.run();
            }
        });

        this.addDataSlot(this.seed).set(playerInventory.player.getEnchantmentSeed());

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot == TOOL_SLOT) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot == INGREDIENT_SLOT) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot == LAPIS_SLOT) {
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (originalStack.is(Items.LAPIS_LAZULI)) {
                if (!this.moveItemStackTo(originalStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (originalStack.isEnchantable() || originalStack.isDamageableItem() || originalStack.is(Items.ENCHANTED_BOOK)){
                    if (!this.slots.get(TOOL_SLOT).mayPlace(originalStack)) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack itemStack = originalStack.copyWithCount(1);
                    originalStack.shrink(1);
                    this.slots.get(TOOL_SLOT).setByPlayer(itemStack);
                } else {
                    if (!this.slots.get(INGREDIENT_SLOT).mayPlace(originalStack)) {
                        return ItemStack.EMPTY;
                    } else if (!this.moveItemStackTo(originalStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, originalStack);
        }
        return newStack;
    }

    public int bookshelfPower(){
        int power = 0;
        for (BlockPos pos : NewEnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            if (NewEnchantingTableBlock.canAccessPowerProvider(blockEntity.getLevel(), blockEntity.getBlockPos(), pos)) {
                power++;
            }
        }
        return power;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 96 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 154));
        }
    }

    public int getSeed() {
        return this.seed.get();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.inventory);
        inventory.setChanged();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            // enchantment button
            if (craftEnchantment()) {
                blockEntity.getLevel().playSound(null, blockEntity.getBlockPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, blockEntity.getLevel().getRandom().nextFloat() * 0.1F + 0.9F);

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ENCHANTED_ITEM.trigger(serverPlayer, inventory.getItem(TOOL_SLOT), id + 1);
                }
            }
            inventory.setChanged();
            return true;
        } else if (id >= 1 && id < getValidEnchantments().size() + 1) {
            // enchantment selector
            // if a button is pressed, automatically move ingredients into their corresponding slots

            String selectedEnchantment = getValidEnchantments().get(id - 1);

            // search for those ingredients in the player's inventory
            if (canAfford(selectedEnchantment)){
                // get the required ingredients for the selected enchantment
                // get recipe
                EnchantmentRecipe recipe = recipeFromEnchantmentId(selectedEnchantment);
                Item ingredient = recipe.inputItem().create().getItem();

                ItemStack itemInIngredientSlot = inventory.getItem(INGREDIENT_SLOT);

                if (!itemInIngredientSlot.is(ingredient)){
                    quickMoveStack(player, INGREDIENT_SLOT);
                }

                // amount needed
                int ingredientCost = recipeIngredientCost(selectedEnchantment);
                int lapisCost = recipeLapisCost(selectedEnchantment);

                // check for necessary ingredients in player inventory
                for (Slot slot : this.slots.subList(4, this.slots.size())) {
                    if (slot.hasItem()){
                        ItemStack originalStack = slot.getItem();

                        // move ingredients into their corresponding slots
                        if (originalStack.is(Items.LAPIS_LAZULI)){
                            if (lapisCost > 0){
                                int originalStackCount = originalStack.getCount();
                                if (lapisCost >= originalStackCount){
                                    // move the whole stack
                                    if (this.moveItemStackTo(originalStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)){
                                        lapisCost -= originalStackCount;
                                    }
                                } else {
                                    // move part of the stack
                                    ItemStack partialStack = originalStack.copyWithCount(lapisCost);
                                    if (this.moveItemStackTo(partialStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)){
                                        originalStack.shrink(lapisCost);
                                        lapisCost = 0;
                                    }
                                }
                            }

                        } else if (originalStack.is(ingredient)){
                            if (ingredientCost > 0){
                                int originalStackCount = originalStack.getCount();
                                if (ingredientCost >= originalStackCount){
                                    // move the whole stack
                                    if (this.moveItemStackTo(originalStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, true)){
                                        ingredientCost -= originalStackCount;
                                    }
                                } else {
                                    // move part of the stack
                                    ItemStack partialStack = originalStack.copyWithCount(ingredientCost);
                                    if (this.moveItemStackTo(partialStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, true)){
                                        originalStack.shrink(ingredientCost);
                                        ingredientCost = 0;
                                    }
                                }
                            }

                        }
                    }
                }
            }

            inventory.setChanged();
            blockEntity.getLevel().playSound(null, blockEntity.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            Util.logAndPauseIfInIde(player.getPlainTextName() + " pressed invalid button id: " + id);
            return false;
        }
    }

    // enchantment recipes only work if the ingredient and output are the same item
    private boolean craftEnchantment(){
        Optional<RecipeHolder<EnchantmentRecipe>> recipe = getCurrentRecipe();

        if (recipe.isPresent()) {

            String enchantmentId = recipe.get().value().output().getRegisteredName();

            if (canEnchantToolSlot(enchantmentId)){

                int currentEnchantmentLevel = getEnchantmentLevel(inventory.getItem(TOOL_SLOT), getEnchantmentFromString(enchantmentId));

                int ingredientCost = recipeIngredientCost(enchantmentId);
                int lapisCost = recipeLapisCost(enchantmentId);

                int ingredientCount = inventory.getItem(INGREDIENT_SLOT).getCount();
                int lapisCount = inventory.getItem(LAPIS_SLOT).getCount();

                // if the enchanting table has enough ingredients to enchant
                if (ingredientCount >= ingredientCost && lapisCount >= lapisCost){
                    inventory.removeItem(INGREDIENT_SLOT, ingredientCost);
                    inventory.removeItem(LAPIS_SLOT, lapisCost);

                    String enchantment = recipe.get().value().output().getRegisteredName();
                    if (!enchantment.isEmpty()){
                        enchantToolSlot(enchantment, currentEnchantmentLevel + 1);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canEnchantToolSlot(String enchantmentId){
        ItemStack tool = inventory.getItem(TOOL_SLOT);

        if (!getValidEnchantments().contains(enchantmentId)){
            return false;
        }

        return canIncreaseEnchantmentLevel(tool, enchantmentId);
    }

    private Holder<Enchantment> getEnchantmentFromString(String enchantmentId){
        Registry<Enchantment> enchantmentRegistry = blockEntity.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        return enchantmentRegistry.wrapAsHolder(enchantmentRegistry.getValue(Identifier.parse(enchantmentId)));
    }

    private void enchantToolSlot(String enchantmentId, int level){
        ItemStack tool = inventory.getItem(TOOL_SLOT);
        ItemStack enchantedBook;
        if (tool.is(Items.BOOK)){
            enchantedBook = tool.transmuteCopy(Items.ENCHANTED_BOOK);
            enchantedBook.enchant(getEnchantmentFromString(enchantmentId), level);
            inventory.setItem(TOOL_SLOT, enchantedBook);
        } else {
            tool.enchant(getEnchantmentFromString(enchantmentId), level);
        }
    }

    // check if the tool slot has a given enchantment
    private boolean hasEnchantment(ItemStack item, String enchantmentId){
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(item));

        for (Holder<Enchantment> entry : builder.keySet()){
            if (entry.getRegisteredName().equals(enchantmentId)){
                return true;
            }
        }

        return false;
    }

    private boolean canIncreaseEnchantmentLevel(ItemStack item, String enchantmentId){
        Holder<Enchantment> enchantment = getEnchantmentFromString(enchantmentId);

        int maxLevel = enchantment.value().getMaxLevel();

        int currentLevel = getEnchantmentLevel(item, enchantment);

        return currentLevel < maxLevel;
    }

    public int getCurrentToolEnchantmentLevel(String enchantmentId){
        return getEnchantmentLevel(inventory.getItem(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
    }

    // check the enchantment level of an enchantment on an item, only use this after a hasEnchantment check
    public static int getEnchantmentLevel(ItemStack item, Holder<Enchantment> enchantment){
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(item));

        for (Holder<Enchantment> entry : builder.keySet()){
            if (entry.getRegisteredName().equals(enchantment.getRegisteredName())){
                return builder.getLevel(entry);
            }
        }

        return 0;
    }

    private Optional<RecipeHolder<EnchantmentRecipe>> getCurrentRecipe() {
        if (blockEntity.getLevel().isClientSide()){
            return Optional.empty();
        }
        return ((ServerLevel) blockEntity.getLevel()).recipeAccess()
                .getRecipeFor(ModRecipes.ENCHANTMENT_TYPE, new EnchantmentRecipeInput(inventory.getItem(INGREDIENT_SLOT)), blockEntity.getLevel());
    }

    // used in NewEnchantmentScreen.java
    public List<String> getValidEnchantments() {
        LinkedList<String> validEnchantments = new LinkedList<>();
        ItemStack tool = inventory.getItem(TOOL_SLOT);

        if (tool.isEmpty()){
            return validEnchantments;
        }

        for (String enchantmentId : listOfEnchantments) {
            if (
                    (tool.canBeEnchantedWith(getEnchantmentFromString(enchantmentId), EnchantingContext.ACCEPTABLE)
                            || tool.is(Items.BOOK)
                            || tool.is(Items.ENCHANTED_BOOK))

                            && canIncreaseEnchantmentLevel(tool, enchantmentId)
                            && isCompatible(tool, enchantmentId)
                            && recipeBookshelfCost(enchantmentId) <= bookshelfPower()
            ){
                validEnchantments.add(enchantmentId);
            }
        }
        return validEnchantments;
    }

    // checks other enchantments on the item to see if they are compatible with the one being applied
    private boolean isCompatible(ItemStack item, String enchantmentId){
        for (String id : listOfEnchantments) {
            if (enchantmentId.equals(id)){
                continue;
            }
            if (hasEnchantment(item, id) && !Enchantment.areCompatible(getEnchantmentFromString(enchantmentId), getEnchantmentFromString(id))){
                return false;
            }
        }
        return true;
    }

    // this is only called if TOOL_SLOT contains an enchantable item
    public boolean canAfford(String enchantmentId){
        if (!canEnchantToolSlot(enchantmentId)){
            return false;
        }

        // get recipe
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);

        if (recipe == null){
            return false;
        }

        // amount needed
        int ingredientCost = recipeIngredientCost(enchantmentId);
        int lapisCost = recipeLapisCost(enchantmentId);

        // amount in inventory
        int ingredientCount = 0;
        int lapisCount = 0;

        // check for necessary ingredients in player inventory
        for (Slot slot : this.slots) {
            if (slot.hasItem()){
                if (slot.getItem().is(Items.LAPIS_LAZULI)){
                    lapisCount += slot.getItem().getCount();
                } else if (slot.getItem().is(recipe.inputItem().create().getItem())){
                    ingredientCount += slot.getItem().getCount();
                }
            }
        }

        return ingredientCount >= ingredientCost && lapisCount >= lapisCost;
    }

    public int recipeIngredientCost(String enchantmentId){
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);
        if (recipe == null) return 0;

        int currentEnchantmentLevel = getEnchantmentLevel(inventory.getItem(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
        // potential index error, only use after a successful call to canIncreaseEnchantmentLevel()
        return recipe.ingredientCost().get(currentEnchantmentLevel);
    }

    public int recipeLapisCost(String enchantmentId){
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);
        // gold tools don't require lapis (NOT TESTED YET)
        if ((recipe == null) || inventory.getItem(TOOL_SLOT).is(ItemTags.PIGLIN_LOVED)) return 0;

        int currentEnchantmentLevel = getEnchantmentLevel(inventory.getItem(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
        // potential index error, only use after a successful call to canIncreaseEnchantmentLevel()
        return recipe.lapis().get(currentEnchantmentLevel);
    }

    public int recipeBookshelfCost(String enchantmentId){
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);
        if (recipe == null) return 0;

        int currentEnchantmentLevel = getEnchantmentLevel(inventory.getItem(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
        // potential index error, only use after a successful call to canIncreaseEnchantmentLevel()
        return recipe.bookshelves().get(currentEnchantmentLevel);
    }

    public static EnchantmentRecipe recipeFromEnchantmentId(String enchantmentId){
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

    public boolean canEnchant(){
        if (inventory.getItem(TOOL_SLOT).isEmpty()){
            return false;
        } else {
            return !getValidEnchantments().isEmpty();
        }
    }

    private static List<String> listOfEnchantments(){
        List<String> enchantmentsList = new ArrayList<>();
        for (EnchantmentRecipe recipe : EnchantmentRecipe.enchantmentRecipes){
            enchantmentsList.add(recipe.output().getRegisteredName());
        }

        return enchantmentsList;
    }

    public void setContentsChangedListener(Runnable contentsChangedListener) {
        this.contentsChangedListener = contentsChangedListener;
    }
}