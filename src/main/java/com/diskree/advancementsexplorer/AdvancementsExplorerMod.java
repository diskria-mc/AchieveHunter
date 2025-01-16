package com.diskree.advancementsexplorer;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.AdvancementEntry;
import org.jetbrains.annotations.NotNull;

public class AdvancementsExplorerMod implements ClientModInitializer {

    public static boolean isClickableAdvancement(@NotNull AdvancementEntry advancementEntry) {
        Advancement advancement = advancementEntry.value();
        AdvancementDisplay display = advancement.display().orElse(null);
        return !advancement.isRoot() && display != null && advancement.requirements().requirements().size() > 1;
    }

    @Override
    public void onInitializeClient() {
    }
}
