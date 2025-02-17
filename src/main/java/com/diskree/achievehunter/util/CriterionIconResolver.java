package com.diskree.achievehunter.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.*;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CriterionIconResolver {

    public static @Nullable CriterionIcon resolveIcon(@NotNull CriterionType type, @NotNull Identifier id) {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return null;
        }
        DynamicRegistryManager registryManager = world.getRegistryManager();
        switch (type) {
            case ITEM, BLOCK -> {
                Item item = Registries.ITEM.get(id);
                if (item != Items.AIR) {
                    return CriterionIcon.ofItem(new ItemStack(item));
                }
            }
            case ENTITY -> {
                SpawnEggItem spawnEggItem = SpawnEggItem.forEntity(Registries.ENTITY_TYPE.get(id));
                if (spawnEggItem != null) {
                    return CriterionIcon.ofItem(new ItemStack(spawnEggItem));
                }
            }
            case VILLAGER_PROFESSION -> {
                PointOfInterestType pointOfInterestType = Registries.POINT_OF_INTEREST_TYPE.get(id);
                if (pointOfInterestType != null && pointOfInterestType.blockStates() != null) {
                    BlockState blockState = new ArrayList<>(pointOfInterestType.blockStates()).getFirst();
                    if (blockState != null) {
                        return CriterionIcon.ofItem(new ItemStack(blockState.getBlock().asItem()));
                    }
                }
            }
            case ENCHANTMENT -> {
                Registry<Enchantment> registry = registryManager.getOrThrow(RegistryKeys.ENCHANTMENT);
                Enchantment enchantment = registry.get(id);
                if (enchantment != null) {
                    return CriterionIcon.ofItem(
                        EnchantmentHelper.getEnchantedBookWith(
                            new EnchantmentLevelEntry(registry.getEntry(enchantment), 1)
                        )
                    );
                }
            }
            case EFFECT -> {
                StatusEffect effect = Registries.STATUS_EFFECT.get(id);
                if (effect != null) {
                    RegistryEntry<StatusEffect> registry = Registries.STATUS_EFFECT.getEntry(effect);
                    if (registry != null) {
                        return CriterionIcon.ofSprite(
                            MinecraftClient.getInstance()
                                .getStatusEffectSpriteManager()
                                .getSprite(registry)
                        );
                    }
                }
            }
            case COLOR -> {
                DyeColor dyeColor = DyeColor.byName(id.getPath(), null);
                if (dyeColor != null) {
                    return CriterionIcon.ofItem(new ItemStack(DyeItem.byColor(dyeColor)));
                }
            }
            case GOAT_HORN -> {
                Registry<Instrument> registry = registryManager.getOrThrow(RegistryKeys.INSTRUMENT);
                Instrument instrument = registry.get(id);
                if (instrument != null) {
                    return CriterionIcon.ofItem(
                        GoatHornItem.getStackForInstrument(
                            Items.GOAT_HORN,
                            registry.getEntry(instrument)
                        )
                    );
                }
            }
            case PAINTING -> {
                Registry<PaintingVariant> registry = registryManager.getOrThrow(RegistryKeys.PAINTING_VARIANT);
                PaintingVariant paintingVariant = registry.get(id);
                if (paintingVariant != null) {
                    ItemStack itemStack = new ItemStack(Items.PAINTING);
                    itemStack.set(
                        DataComponentTypes.PAINTING_VARIANT,
                        registry.getEntry(paintingVariant)
                    );
                    return CriterionIcon.ofItem(itemStack);
                }
            }
        }
        return null;
    }
}
