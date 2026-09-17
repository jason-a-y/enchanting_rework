package net.cree_per_.enchanting_rework.screen.custom;

import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipe;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.cree_per_.enchanting_rework.block.custom.NewEnchantingTableBlock;
import net.cree_per_.enchanting_rework.block.entity.custom.NewEnchantingTableBlockEntity;
import net.cree_per_.enchanting_rework.recipe.EnchantmentRecipeInput;
import net.cree_per_.enchanting_rework.recipe.ModRecipes;
import net.cree_per_.enchanting_rework.screen.ModScreenHandlers;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class NewEnchantmentScreenHandler extends ScreenHandler {
    static final Identifier EMPTY_LAPIS_LAZULI_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/lapis_lazuli");
    private final Inventory inventory;
    public final NewEnchantingTableBlockEntity blockEntity;

    public static final List<String> listOfEnchantments = listOfEnchantments();

    private final Property seed = Property.create();

    private static final int TOOL_SLOT = 0;
    private static final int INGREDIENT_SLOT = 1;
    private static final int LAPIS_SLOT = 2;

    Runnable contentsChangedListener = () -> {};

    public NewEnchantmentScreenHandler(int syncId, PlayerInventory inventory, BlockPos pos) {
        this(syncId, inventory, inventory.player.getEntityWorld().getBlockEntity(pos));
    }

    public NewEnchantmentScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        super(ModScreenHandlers.NEW_ENCHANTMENT_SCREEN_HANDLER, syncId);
        this.inventory = ((Inventory) blockEntity);
        this.blockEntity = ((NewEnchantingTableBlockEntity) blockEntity);

        // tool slot
        this.addSlot(new Slot(inventory, 0, 15, 47){
            @Override
            public int getMaxItemCount() {
                return 1;
            }

            @Override
            public void markDirty() {
                super.markDirty();
                NewEnchantmentScreenHandler.this.contentsChangedListener.run();
            }
        });
        // ingredient slot
        this.addSlot(new Slot(inventory, 1, 35, 47){
            @Override
            public void markDirty() {
                super.markDirty();
                NewEnchantmentScreenHandler.this.contentsChangedListener.run();
            }
        });
        // lapis slot
        this.addSlot(new Slot(inventory, 2, 15, 67){
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.LAPIS_LAZULI);
            }

            @Override
            public Identifier getBackgroundSprite() {
                return NewEnchantmentScreenHandler.EMPTY_LAPIS_LAZULI_SLOT_TEXTURE;
            }

            @Override
            public void markDirty() {
                super.markDirty();
                NewEnchantmentScreenHandler.this.contentsChangedListener.run();
            }
        });

        this.addProperty(this.seed).set(playerInventory.player.getEnchantingTableSeed());

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot == TOOL_SLOT) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot == INGREDIENT_SLOT) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (invSlot == LAPIS_SLOT) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (originalStack.isOf(Items.LAPIS_LAZULI)) {
                if (!this.insertItem(originalStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (originalStack.isEnchantable() || originalStack.isDamageable() || originalStack.isOf(Items.ENCHANTED_BOOK)){
                    if (this.slots.get(TOOL_SLOT).hasStack() || !this.slots.get(TOOL_SLOT).canInsert(originalStack)) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack itemStack = originalStack.copyWithCount(1);
                    originalStack.decrement(1);
                    this.slots.get(TOOL_SLOT).setStack(itemStack);
                } else {
                    if (this.slots.get(INGREDIENT_SLOT).hasStack() || !this.slots.get(INGREDIENT_SLOT).canInsert(originalStack)) {
                        return ItemStack.EMPTY;
                    } else if (!this.insertItem(originalStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, true)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    public int bookshelfPower(){
        int power = 0;
        for (BlockPos pos : NewEnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            if (NewEnchantingTableBlock.canAccessPowerProvider(blockEntity.getWorld(), blockEntity.getPos(), pos)) {
                power++;
            }
        }
        return power;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 96 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 154));
        }
    }

    public int getSeed() {
        return this.seed.get();
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, this.inventory);
        inventory.markDirty();
    }

    @Override
    public boolean onButtonClick(PlayerEntity player, int id) {
        if (id == 0) {
            // enchantment button
            if (craftEnchantment()) {
                blockEntity.getWorld().playSound(null, blockEntity.getPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, blockEntity.getWorld().random.nextFloat() * 0.1F + 0.9F);

                player.incrementStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    Criteria.ENCHANTED_ITEM.trigger(serverPlayer, inventory.getStack(TOOL_SLOT), id + 1);
                }
            }
            inventory.markDirty();
            return true;
        } else if (id >= 1 && id < getValidEnchantments().size() + 1) {
            quickMove(player, INGREDIENT_SLOT);
            quickMove(player, LAPIS_SLOT);
            // enchantment selector
            // if a button is pressed, automatically move ingredients into their corresponding slots

            String selectedEnchantment = getValidEnchantments().get(id - 1);

            // search for those ingredients in the player's inventory
            if (canAfford(selectedEnchantment)){
                // get the required ingredients for the selected enchantment
                // get recipe
                EnchantmentRecipe recipe = recipeFromEnchantmentId(selectedEnchantment);

                if (recipe == null){
                    return false;
                }

                // amount needed
                int ingredientCost = recipeIngredientCost(selectedEnchantment);
                int lapisCost = recipeLapisCost(selectedEnchantment);

                // check for necessary ingredients in player inventory
                for (Slot slot : this.slots.subList(4, this.slots.size())) {
                    if (slot.hasStack()){
                        ItemStack originalStack = slot.getStack();

                        // to do: move ingredients into their corresponding slots
                        if (originalStack.isOf(Items.LAPIS_LAZULI)){
                            if (lapisCost > 0){
                                int originalStackCount = originalStack.getCount();
                                if (lapisCost >= originalStackCount){
                                    // move the whole stack
                                    if (this.insertItem(originalStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)){
                                        lapisCost -= originalStackCount;
                                    }
                                } else {
                                    // move part of the stack
                                    ItemStack partialStack = originalStack.copyWithCount(lapisCost);
                                    if (this.insertItem(partialStack, LAPIS_SLOT, LAPIS_SLOT + 1, true)){
                                        originalStack.decrement(lapisCost);
                                        lapisCost = 0;
                                    }
                                }
                            }

                        } else if (originalStack.isOf(recipe.inputItem().getItem())){
                            if (ingredientCost > 0){
                                int originalStackCount = originalStack.getCount();
                                if (ingredientCost >= originalStackCount){
                                    // move the whole stack
                                    if (this.insertItem(originalStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, true)){
                                        ingredientCost -= originalStackCount;
                                    }
                                } else {
                                    // move part of the stack
                                    ItemStack partialStack = originalStack.copyWithCount(ingredientCost);
                                    if (this.insertItem(partialStack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, true)){
                                        originalStack.decrement(ingredientCost);
                                        ingredientCost = 0;
                                    }
                                }
                            }

                        }
                    }
                }
            }

            inventory.markDirty();
            blockEntity.getWorld().playSound(null, blockEntity.getPos(), SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        } else {
            Util.logErrorOrPause(player.getStringifiedName() + " pressed invalid button id: " + id);
            return false;
        }
    }

    // enchantment recipes only work if the ingredient and output are the same item
    private boolean craftEnchantment(){
        Optional<RecipeEntry<EnchantmentRecipe>> recipe = getCurrentRecipe();

        if (recipe.isPresent()) {

            String enchantmentId = recipe.get().value().output().getIdAsString();

            if (canEnchantToolSlot(enchantmentId)){

                int currentEnchantmentLevel = getEnchantmentLevel(inventory.getStack(TOOL_SLOT), getEnchantmentFromString(enchantmentId));

                int ingredientCost = recipeIngredientCost(enchantmentId);
                int lapisCost = recipeLapisCost(enchantmentId);

                int ingredientCount = inventory.getStack(INGREDIENT_SLOT).getCount();
                int lapisCount = inventory.getStack(LAPIS_SLOT).getCount();

                // if the enchanting table has enough ingredients to enchant
                if (ingredientCount >= ingredientCost && lapisCount >= lapisCost){
                    inventory.removeStack(INGREDIENT_SLOT, ingredientCost);
                    inventory.removeStack(LAPIS_SLOT, lapisCost);

                    String enchantment = recipe.get().value().output().getIdAsString();
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
        ItemStack tool = inventory.getStack(TOOL_SLOT);

        if (!getValidEnchantments().contains(enchantmentId)){
            return false;
        }

        return canIncreaseEnchantmentLevel(tool, enchantmentId);
    }

    private RegistryEntry<Enchantment> getEnchantmentFromString(String enchantmentId){
        Registry<Enchantment> enchantmentRegistry = blockEntity.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        return enchantmentRegistry.getEntry(enchantmentRegistry.get(Identifier.of(enchantmentId)));
    }

    private void enchantToolSlot(String enchantmentId, int level){
        ItemStack tool = inventory.getStack(TOOL_SLOT);
        ItemStack enchantedBook;
        if (tool.isOf(Items.BOOK)){
            enchantedBook = tool.withItem(Items.ENCHANTED_BOOK);
            enchantedBook.addEnchantment(getEnchantmentFromString(enchantmentId), level);
            inventory.setStack(TOOL_SLOT, enchantedBook);
        } else {
            tool.addEnchantment(getEnchantmentFromString(enchantmentId), level);
        }
    }

    // check if the tool slot has a given enchantment
    private boolean hasEnchantment(ItemStack item, String enchantmentId){
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(item));

        for (RegistryEntry<Enchantment> entry : builder.getEnchantments()){
            if (entry.getIdAsString().equals(enchantmentId)){
                return true;
            }
        }

        return false;
    }

    private boolean canIncreaseEnchantmentLevel(ItemStack item, String enchantmentId){
        RegistryEntry<Enchantment> enchantment = getEnchantmentFromString(enchantmentId);

        int maxLevel = enchantment.value().getMaxLevel();

        int currentLevel = getEnchantmentLevel(item, enchantment);

        return currentLevel < maxLevel;
    }

    public int getCurrentToolEnchantmentLevel(String enchantmentId){
        return getEnchantmentLevel(inventory.getStack(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
    }

    // check the enchantment level of an enchantment on an item, only use this after a hasEnchantment check
    public static int getEnchantmentLevel(ItemStack item, RegistryEntry<Enchantment> enchantment){
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(EnchantmentHelper.getEnchantments(item));

        for (RegistryEntry<Enchantment> entry : builder.getEnchantments()){
            if (entry.getIdAsString().equals(enchantment.getIdAsString())){
                return builder.getLevel(entry);
            }
        }

        return 0;
    }

    private Optional<RecipeEntry<EnchantmentRecipe>> getCurrentRecipe() {
        if (blockEntity.getWorld().isClient()){
            return Optional.empty();
        }
        return ((ServerWorld) blockEntity.getWorld()).getRecipeManager()
                .getFirstMatch(ModRecipes.ENCHANTMENT_TYPE, new EnchantmentRecipeInput(inventory.getStack(INGREDIENT_SLOT)), blockEntity.getWorld());
    }

    // used in NewEnchantmentScreen.java
    public List<String> getValidEnchantments() {
        LinkedList<String> validEnchantments = new LinkedList<>();
        ItemStack tool = inventory.getStack(TOOL_SLOT);

        if (tool.isEmpty()){
            return validEnchantments;
        }

        for (String enchantmentId : listOfEnchantments) {
            if (
                    (tool.canBeEnchantedWith(getEnchantmentFromString(enchantmentId), EnchantingContext.ACCEPTABLE)
                            || tool.isOf(Items.BOOK)
                            || tool.isOf(Items.ENCHANTED_BOOK))

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
            if (hasEnchantment(item, id) && !Enchantment.canBeCombined(getEnchantmentFromString(enchantmentId), getEnchantmentFromString(id))){
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
            if (slot.hasStack()){
                if (slot.getStack().isOf(Items.LAPIS_LAZULI)){
                    lapisCount += slot.getStack().getCount();
                } else if (slot.getStack().isOf(recipe.inputItem().getItem())){
                    ingredientCount += slot.getStack().getCount();
                }
            }
        }

        return ingredientCount >= ingredientCost && lapisCount >= lapisCost;
    }

    public int recipeIngredientCost(String enchantmentId){
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);
        if (recipe == null) return 0;

        int currentEnchantmentLevel = getEnchantmentLevel(inventory.getStack(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
        // potential index error, only use after a successful call to canIncreaseEnchantmentLevel()
        return recipe.ingredientCost().get(currentEnchantmentLevel);
    }

    public int recipeLapisCost(String enchantmentId){
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);
        // gold tools don't require lapis (NOT TESTED YET)
        if ((recipe == null) || inventory.getStack(TOOL_SLOT).isIn(ItemTags.PIGLIN_LOVED)) return 0;

        int currentEnchantmentLevel = getEnchantmentLevel(inventory.getStack(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
        // potential index error, only use after a successful call to canIncreaseEnchantmentLevel()
        return recipe.lapis().get(currentEnchantmentLevel);
    }

    public int recipeBookshelfCost(String enchantmentId){
        EnchantmentRecipe recipe = recipeFromEnchantmentId(enchantmentId);
        if (recipe == null) return 0;

        int currentEnchantmentLevel = getEnchantmentLevel(inventory.getStack(TOOL_SLOT), getEnchantmentFromString(enchantmentId));
        // potential index error, only use after a successful call to canIncreaseEnchantmentLevel()
        return recipe.bookshelves().get(currentEnchantmentLevel);
    }

    public static EnchantmentRecipe recipeFromEnchantmentId(String enchantmentId){
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

    public boolean canEnchant(){
        if (inventory.getStack(TOOL_SLOT).isEmpty()){
            return false;
        } else {
            return !getValidEnchantments().isEmpty();
        }
    }

    private static List<String> listOfEnchantments(){
        List<String> enchantmentsList = new ArrayList<>();
        for (EnchantmentRecipe recipe : EnchantmentRecipe.enchantmentRecipes){
            enchantmentsList.add(recipe.output().getIdAsString());
        }

        return enchantmentsList;
    }

    public void setContentsChangedListener(Runnable contentsChangedListener) {
        this.contentsChangedListener = contentsChangedListener;
    }
}