package com.diskree.achievehunter.gui;

import com.diskree.achievehunter.util.CriterionIcon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
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
        boolean isObtained,
        int itemHeight,
        int headerHeight
    ) {
        super(client, 0, 0, 0, itemHeight, headerHeight);
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

    public void setCriteria(@NotNull List<AdvancementCriterionItem> criteria) {
        clearEntries();
        for (AdvancementCriterionItem criterion : criteria) {
            addEntry(new CriterionEntry(criterion));
        }
        setScrollY(0);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Environment(EnvType.CLIENT)
    public class CriterionEntry extends EntryListWidget.Entry<CriterionEntry> {

        private final AdvancementCriterionItem criterion;

        public CriterionEntry(AdvancementCriterionItem criterion) {
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
            CriterionIcon icon = criterion.icon();
            if (icon.isItem()) {
                int iconSize = 16;
                int iconMargin = (entryHeight - iconSize) / 2;
                context.drawItem(
                    icon.stack(),
                    x + iconMargin,
                    y + iconMargin
                );
            } else if (icon.isSprite()) {
                Sprite sprite = icon.sprite();
                if (sprite != null) {
                    int iconSize = Math.max(sprite.getContents().getWidth(), sprite.getContents().getHeight());
                    int iconMargin = (entryHeight - iconSize) / 2;
                    context.drawSpriteStretched(
                        RenderLayer::getGuiTextured,
                        sprite,
                        x + iconMargin,
                        y + iconMargin,
                        iconSize,
                        iconSize
                    );
                }
            }

            int textLeftMargin = icon == CriterionIcon.NO_ICON ? 2 : entryHeight + 4;
            context.drawText(
                client.textRenderer,
                criterion.displayedText(),
                x + textLeftMargin,
                y + 6,
                isObtained ? Colors.GRAY : Colors.WHITE,
                false
            );

//            context.drawHorizontalLine(x, x + entryWidth, y, 0xffff0000);
//            context.drawHorizontalLine(x, x + entryWidth, y + entryHeight - 1, 0xff00ff00);
        }
    }
}
