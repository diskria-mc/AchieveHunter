package com.diskree.achievehunter.util;

import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CriterionIcon(@Nullable ItemStack stack, @Nullable Sprite sprite) {

    public static @NotNull CriterionIcon ofItem(@NotNull ItemStack stack) {
        return new CriterionIcon(stack, null);
    }

    public static @NotNull CriterionIcon ofSprite(@NotNull Sprite sprite) {
        return new CriterionIcon(null, sprite);
    }

    public boolean isItem() {
        return stack != null;
    }

    public boolean isSprite() {
        return sprite != null;
    }
}
