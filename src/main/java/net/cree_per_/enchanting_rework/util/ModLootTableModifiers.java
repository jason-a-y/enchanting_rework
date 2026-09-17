package net.cree_per_.enchanting_rework.util;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

public class ModLootTableModifiers {

    public static void modifyLootTables() {
        LootTableEvents.REPLACE.register((registryKey, lootTable, lootTableSource, wrapperLookup) -> {
            RegistryWrapper.Impl<Enchantment> impl = wrapperLookup.getOrThrow(RegistryKeys.ENCHANTMENT);

            return null;
        });
    }
}
