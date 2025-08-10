package com.diskria.achievehunter.injection.mixin;

import com.diskria.achievehunter.gui.AdvancementCriteriaListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntryListWidget.class)
public class EntryListWidgetMixin {

    @SuppressWarnings("rawtypes")
    @ModifyConstant(
            method = {
                    "getEntryAtPosition",
                    "getContentsHeightWithPadding",
                    "ensureVisible",
                    "renderList",
                    "getRowTop",
            },
            constant = @Constant(intValue = 4)
    )
    private int removeOffsetInCriteriaListWidget(int originalValue) {
        EntryListWidget entryListWidget = (EntryListWidget) (Object) this;
        return entryListWidget instanceof AdvancementCriteriaListWidget ? 0 : originalValue;
    }
}
