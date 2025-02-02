package com.diskree.achievehunter.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Environment(EnvType.CLIENT)
public class AdvancementCriteriaListWidget extends EntryListWidget<AdvancementCriteriaListWidget.CriterionEntry> {

    private final boolean isObtained;

    public AdvancementCriteriaListWidget(
        MinecraftClient client,
        int width,
        int height,
        int topY,
        boolean isObtained
    ) {
        super(client, width, height, topY, 16, (int) (9.0f * 1.5f));
        this.isObtained = isObtained;
    }

    @Override
    protected void renderHeader(@NotNull DrawContext context, int x, int y) {
        Text text = Text
            .translatable(
                isObtained
                    ? "achievehunter.criteria_header.obtained"
                    : "achievehunter.criteria_header.unobtained"
            )
            .formatted(Formatting.UNDERLINE, Formatting.BOLD);
        context.drawTextWithShadow(
            client.textRenderer,
            text,
            x + width / 2 - client.textRenderer.getWidth(text) / 2,
            Math.min(getY() + 3, y),
            Colors.WHITE
        );
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    protected int getScrollbarX() {
        return getRight() - 6;
    }

    public void setCriteria(@NotNull List<AdvancementCriterion> criteria) {
        clearEntries();
        for (AdvancementCriterion criterion : criteria) {
            addEntry(new CriterionEntry(criterion));
        }
        setScrollY(0);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Environment(EnvType.CLIENT)
    public class CriterionEntry extends EntryListWidget.Entry<CriterionEntry> {

        private final AdvancementCriterion criterion;

        public CriterionEntry(AdvancementCriterion criterion) {
            this.criterion = criterion;
        }

        @Override
        public void render(
            @NotNull DrawContext context,
            int index,
            int y,
            int x,
            int entryWidth,
            int entryHeight,
            int mouseX,
            int mouseY,
            boolean hovered,
            float tickDelta
        ) {
            context.drawText(
                client.textRenderer,
                criterion.name(),
                x,
                y,
                isObtained ? Colors.GRAY : Colors.WHITE,
                false
            );
        }
    }
}
