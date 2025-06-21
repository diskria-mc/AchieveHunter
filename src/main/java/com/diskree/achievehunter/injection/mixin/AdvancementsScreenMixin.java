package com.diskree.achievehunter.injection.mixin;

import com.diskree.achievehunter.AchieveHunterMod;
import com.diskree.achievehunter.Constants;
import com.diskree.achievehunter.gui.AdvancementCriteriaListWidget;
import com.diskree.achievehunter.gui.AdvancementCriterionItem;
import com.diskree.achievehunter.injection.extension.AdvancementWidgetExtension;
import com.diskree.achievehunter.injection.extension.AdvancementsScreenExtension;
import com.diskree.achievehunter.util.*;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancement.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.client.gui.screen.advancement.AdvancementTabType;
import net.minecraft.client.gui.screen.advancement.AdvancementWidget;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.client.gui.screen.advancement.AdvancementsScreen.*;
import static net.minecraft.client.gui.widget.EntryListWidget.INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
import static net.minecraft.client.gui.widget.EntryListWidget.MENU_LIST_BACKGROUND_TEXTURE;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin extends Screen implements AdvancementsScreenExtension {

    @Unique
    private static final Text SEARCH_TITLE = Text.translatable("gui.recipebook.search_hint");

    @Unique
    private static final int FOCUSED_ADVANCEMENT_CLICK_TIMEOUT_TICKS = 10;

    @Unique
    private static final double DRAG_THRESHOLD = 5.0d;

    @Unique
    private static final float ANIMATION_DURATION_SECONDS = 0.4f;

    @Unique
    private static final int SEARCH_FIELD_TEXT_LEFT_OFFSET = 2;

    @Unique
    private static final int MIN_ADVANCEMENTS_LIST_WIDTH = 250;

    @Unique
    private static final int MIN_CRITERIA_LIST_WIDGET_WIDTH = 110;

    @Unique
    private static final int CRITERIA_LIST_MARGIN = 12;

    @Unique
    private static final Rectangle SEARCH_FIELD_RECT = new Rectangle(80, 4, 90, 12);

    @Unique
    private static final int WIDGET_SIZE = 26;

    @Unique
    private static final int TREE_X_OFFSET = 3;

    @Unique
    private static final int WIDGET_HIGHLIGHT_COUNT = 5;

    @Unique
    private static final int WIDGET_HIGHLIGHT_TICKS = 3;

    @Unique
    private static final int CRITERIA_HEADER_HEIGHT = 13;

    @Unique
    private static final int CRITERIA_ITEM_HEIGHT = 20;

    @Unique
    private TextFieldWidget advancementsSearchField;

    @Unique
    private PlacedAdvancement searchRootAdvancement;

    @Unique
    private AdvancementTab searchTab;

    @Unique
    private final List<PlacedAdvancement> searchResults = new ArrayList<>();

    @Unique
    private boolean isAdvancementsSearchActive;

    @Unique
    private boolean isCriteriaSearchActive;

    @Unique
    private int advancementsSearchResultsColumnsCount;

    @Unique
    private int advancementsSearchResultsOriginX;

    @Unique
    private AdvancementWidget focusedAdvancementWidget;

    @Unique
    private PlacedAdvancement highlightedAdvancement;

    @Unique
    private Identifier highlightedAdvancementId;

    @Unique
    private HighlightType highlightType;

    @Unique
    private int widgetHighlightCounter;

    @Unique
    private AdvancementEntry selectedAdvancement;

    @Unique
    private boolean isFocusedAdvancementClicked;

    @Unique
    private int obtainedAdvancementsCount;

    @Unique
    private int totalAdvancementsCount;

    @Unique
    private static final List<AdvancementFrame> FRAME_TYPE_PRIORITY = Arrays.asList(
        AdvancementFrame.TASK,
        AdvancementFrame.GOAL,
        AdvancementFrame.CHALLENGE
    );

    @Unique
    private boolean isCriteriaOpened;

    @Unique
    @Nullable
    private PlacedAdvancement focusedPlacedAdvancement;

    @Unique
    private double focusedAdvancementClickX;

    @Unique
    private double focusedAdvancementClickY;

    @Unique
    private int focusedAdvancementReleaseClickTimer;

    @Unique
    @Nullable
    private AdvancementWidget transitionAdvancementWidget;

    @Unique
    private int tooltipTransitionStartX;

    @Unique
    private int tooltipTransitionStartY;

    @Unique
    private int tooltipTransitionEndX;

    @Unique
    private int tooltipTransitionEndY;

    @Unique
    private boolean shouldHorizontallySwapTransitionTooltip;

    @Unique
    private boolean shouldVerticallySwapTransitionTooltip;

    @Unique
    private List<PlacedAdvancement> advancementsWithProgress;

    @Unique
    private final List<PlacedAdvancement> criteriaSearchResults = new ArrayList<>();

    @Unique
    private final Map<PlacedAdvancement, AdvancementWidget> advancementWidgetsCache = new HashMap<>();

    @Unique
    private int currentAdvancementIndex = -1;

    @Unique
    private int currentCriteriaSearchResultIndex = -1;

    @Unique
    private TextFieldWidget criteriaSearchField;

    @Unique
    private AdvancementCriteriaListWidget unobtainedCriteriaListWidget;

    @Unique
    private AdvancementCriteriaListWidget obtainedCriteriaListWidget;

    @Unique
    private final FullscreenAdvancementsWindow fullscreenAdvancementsWindow = new FullscreenAdvancementsWindow();

    @Unique
    private int windowX;

    @Unique
    private int windowY;

    @Unique
    private int windowWidth;

    @Unique
    private int windowHeight;

    @Unique
    private int windowHorizontalMargin;

    @Unique
    private int windowVerticalMargin;

    protected AdvancementsScreenMixin(Text title) {
        super(title);
    }

    @Unique
    private void setSelectedAdvancement(AdvancementEntry selectedAdvancement) {
        this.selectedAdvancement = selectedAdvancement;
        if (selectedAdvancement == null) {
            unobtainedCriteriaListWidget.visible = false;
            obtainedCriteriaListWidget.visible = false;
            return;
        }
        List<AdvancementCriterionItem> unobtainedCriteria = getCriteria(selectedAdvancement, false);
        List<AdvancementCriterionItem> obtainedCriteria = getCriteria(selectedAdvancement, true);

        boolean isUnobtainedVisible = !unobtainedCriteria.isEmpty();
        boolean isObtainedVisible = !obtainedCriteria.isEmpty();

        unobtainedCriteriaListWidget.visible = isUnobtainedVisible;
        obtainedCriteriaListWidget.visible = isObtainedVisible;

        if (!isUnobtainedVisible && !isObtainedVisible) {
            return;
        }

        int advancementsListRight = getAdvancementsListWidth();
        int criteriaListWidth = (width - advancementsListRight - CRITERIA_LIST_MARGIN * 3) / 2;
        int footerTop = height - PAGE_OFFSET_X;
        int criteriaSectionTop = PAGE_OFFSET_Y + CRITERIA_LIST_MARGIN;
        int criteriaSectionBottom = footerTop - CRITERIA_LIST_MARGIN;
        int availableHeightForItems = criteriaSectionBottom - criteriaSectionTop - CRITERIA_HEADER_HEIGHT;
        int maxItems = availableHeightForItems / CRITERIA_ITEM_HEIGHT;
        int criteriaListHeight = CRITERIA_HEADER_HEIGHT + maxItems * CRITERIA_ITEM_HEIGHT;
        int criteriaListX = advancementsListRight + CRITERIA_LIST_MARGIN;
        int criteriaListY = criteriaSectionTop + (criteriaSectionBottom - criteriaSectionTop - criteriaListHeight) / 2;

        if (isUnobtainedVisible && isObtainedVisible) {
            unobtainedCriteriaListWidget.position(criteriaListWidth, criteriaListHeight, 0);
            unobtainedCriteriaListWidget.setX(criteriaListX);
            unobtainedCriteriaListWidget.setY(criteriaListY);
            unobtainedCriteriaListWidget.setCriteria(unobtainedCriteria);

            obtainedCriteriaListWidget.position(criteriaListWidth, criteriaListHeight, 0);
            obtainedCriteriaListWidget.setX(criteriaListX + criteriaListWidth + CRITERIA_LIST_MARGIN);
            obtainedCriteriaListWidget.setY(criteriaListY);
            obtainedCriteriaListWidget.setCriteria(obtainedCriteria);
        } else {
            AdvancementCriteriaListWidget criteriaListWidget = isUnobtainedVisible
                ? unobtainedCriteriaListWidget
                : obtainedCriteriaListWidget;
            List<AdvancementCriterionItem> criteria = isUnobtainedVisible ? unobtainedCriteria : obtainedCriteria;
            criteriaListWidget.position(criteriaListWidth * 2 + CRITERIA_LIST_MARGIN, criteriaListHeight, 0);
            criteriaListWidget.setX(criteriaListX);
            criteriaListWidget.setY(criteriaListY);
            criteriaListWidget.setCriteria(criteria);
        }
    }

    @Unique
    private @NotNull List<AdvancementCriterionItem> getCriteria(
        @NotNull AdvancementEntry advancementEntry,
        boolean filterByObtained
    ) {
        List<AdvancementCriterionItem> criteria = new ArrayList<>();
        AdvancementProgress progress = advancementHandler.advancementProgresses.get(advancementEntry);
        for (List<String> criteriaNames : advancementEntry.value().requirements().requirements()) {
            if (criteriaNames.isEmpty()) {
                continue;
            }
            boolean isAnyObtained = false;
            for (String criterionName : criteriaNames) {
                if (progress.isCriterionObtained(criterionName)) {
                    isAnyObtained = true;
                    break;
                }
            }
            if (filterByObtained == isAnyObtained) {
                String technicallyCriterionName = criteriaNames.size() == 1
                    ? criteriaNames.getFirst()
                    : Collections.min(criteriaNames);

                Criterion criterion = HumanReadableCriterionHelper.getHumanReadableCriterion(
                    advancementEntry.id(),
                    technicallyCriterionName
                );
                String criterionName = criterion.criterionName();
                AdvancementCriterionItem criterionItem;
                if (criterionName == null) {
                    criterionItem = new AdvancementCriterionItem(
                        "",
                        Text.literal("null").formatted(Formatting.RED),
                        criterion.icon()
                    );
                } else {
                    criterionItem = new AdvancementCriterionItem(
                        criterionName.toLowerCase(Locale.ROOT),
                        getCriterionNameText(criterion.criterionName()),
                        criterion.icon()
                    );
                }
                criteria.add(criterionItem);
            }
        }
        if (isCriteriaSearchActive) {
            String query = criteriaSearchField.getText().toLowerCase(Locale.ROOT);
            if (!query.isEmpty()) {
                List<AdvancementCriterionItem> matchingCriteria = new ArrayList<>();
                List<AdvancementCriterionItem> otherCriteria = new ArrayList<>();

                for (AdvancementCriterionItem criterion : criteria) {
                    if (criterion.rawName().contains(query)) {
                        matchingCriteria.add(criterion);
                    } else {
                        otherCriteria.add(criterion);
                    }
                }

                matchingCriteria.sort(Comparator.comparingInt(criterion -> criterion.rawName().indexOf(query)));
                otherCriteria.sort(Comparator.comparing(AdvancementCriterionItem::rawName));

                criteria = new ArrayList<>();
                criteria.addAll(matchingCriteria);
                criteria.addAll(otherCriteria);
            }
        } else {
            criteria.sort(Comparator.comparing(AdvancementCriterionItem::rawName));
        }
        return criteria;
    }

    @Unique
    private @NotNull Text getCriterionNameText(@NotNull String criterionName) {
        String query = criteriaSearchField.getText().toLowerCase(Locale.ROOT);

        int highlightStartIndex = isCriteriaSearchActive && !query.isEmpty()
            ? criterionName.toLowerCase(Locale.ROOT).indexOf(query)
            : -1;

        if (highlightStartIndex != -1) {
            int highlightEndIndex = highlightStartIndex + query.length();
            return Text
                .literal(criterionName.substring(0, highlightStartIndex))
                .append(
                    Text
                        .literal(criterionName.substring(highlightStartIndex, highlightEndIndex))
                        .formatted(Formatting.YELLOW)
                        .formatted(Formatting.UNDERLINE)
                )
                .append(Text.literal(criterionName.substring(highlightEndIndex)));
        }
        return Text.literal(criterionName);
    }

    @Unique
    private void setCurrentAdvancementIndex(int index) {
        List<PlacedAdvancement> advancementsWithProgress = getAdvancementsWithProgress();
        if (index < 0 || index >= advancementsWithProgress.size()) {
            return;
        }
        currentAdvancementIndex = index;
        setSelectedAdvancement(advancementsWithProgress.get(currentAdvancementIndex).getAdvancementEntry());
    }

    @Unique
    private void setCurrentCriteriaSearchResultIndex(int index) {
        if (index < 0 || index >= criteriaSearchResults.size()) {
            return;
        }
        currentCriteriaSearchResultIndex = index;
        setSelectedAdvancement(criteriaSearchResults.get(currentCriteriaSearchResultIndex).getAdvancementEntry());
    }

    @Unique
    private @NotNull List<PlacedAdvancement> getAdvancementsWithProgress() {
        if (advancementsWithProgress == null) {
            advancementsWithProgress = new ArrayList<>();
            for (PlacedAdvancement advancement : new ArrayList<>(advancementHandler.getManager().getAdvancements())) {
                if (shouldIncludeAdvancement(advancement)) {
                    advancementsWithProgress.add(advancement);
                }
            }
            Map<AdvancementEntry, AdvancementProgress> progresses = advancementHandler.advancementProgresses;

            obtainedAdvancementsCount = 0;
            totalAdvancementsCount = 0;
            for (PlacedAdvancement placedAdvancement : advancementsWithProgress) {
                totalAdvancementsCount++;
                if (progresses.get(placedAdvancement.getAdvancementEntry()).isDone()) {
                    obtainedAdvancementsCount++;
                }
            }

            advancementsWithProgress.sort((advancement, otherAdvancement) -> {
                AdvancementProgress progress = progresses.get(advancement.getAdvancementEntry());
                AdvancementProgress otherProgress = progresses.get(otherAdvancement.getAdvancementEntry());
                int totalRequirementsCount = progress.requirements.getLength();
                int otherTotalRequirementsCount = otherProgress.requirements.getLength();
                int obtainedRequirementsCount = progress.countObtainedRequirements();
                int otherObtainedRequirementsCount = otherProgress.countObtainedRequirements();
                boolean isDone = progress.isDone();
                boolean otherIsDone = otherProgress.isDone();
                boolean isAnyObtained = progress.isAnyObtained();
                boolean otherIsAnyObtained = otherProgress.isAnyObtained();
                boolean isStarted = isAnyObtained && !isDone;
                boolean otherIsStarted = otherIsAnyObtained && !otherIsDone;

                if (isStarted && otherIsStarted) {
                    int remainingRequirementsCountComparison = Integer.compare(
                        totalRequirementsCount - obtainedRequirementsCount,
                        otherTotalRequirementsCount - otherObtainedRequirementsCount
                    );
                    if (remainingRequirementsCountComparison != 0) {
                        return remainingRequirementsCountComparison;
                    }
                }
                if (isStarted) {
                    return -1;
                }
                if (otherIsStarted) {
                    return 1;
                }

                boolean isNotStarted = !isAnyObtained && !isDone;
                boolean otherIsNotStarted = !otherIsAnyObtained && !otherIsDone;
                if (!isNotStarted) {
                    if (otherIsNotStarted) {
                        return 1;
                    }
                } else if (!otherIsNotStarted) {
                    return -1;
                }

                AdvancementDisplay display = advancement.getAdvancement().display().orElse(null);
                AdvancementDisplay nextDisplay = otherAdvancement.getAdvancement().display().orElse(null);
                if (display == null || nextDisplay == null) {
                    return 0;
                }
                int frameIndex = FRAME_TYPE_PRIORITY.indexOf(display.getFrame());
                int nextFrameIndex = FRAME_TYPE_PRIORITY.indexOf(nextDisplay.getFrame());
                int frameIndexComparison = Integer.compare(frameIndex, nextFrameIndex);
                if (frameIndexComparison != 0) {
                    return frameIndexComparison;
                }
                int totalRequirementsCountComparison = Integer.compare(
                    totalRequirementsCount,
                    otherTotalRequirementsCount
                );
                if (totalRequirementsCountComparison != 0) {
                    return totalRequirementsCountComparison;
                }
                return advancement.getAdvancementEntry().id().compareTo(otherAdvancement.getAdvancementEntry().id());
            });
        }
        return advancementsWithProgress;
    }

    @Unique
    private boolean shouldIncludeAdvancement(@Nullable PlacedAdvancement placedAdvancement) {
        if (placedAdvancement == null) {
            return false;
        }
        AdvancementEntry advancementEntry = placedAdvancement.getAdvancementEntry();
        Advancement advancement = advancementEntry.value();
        if (advancement.isRoot()) {
            return false;
        }
        AdvancementDisplay display = advancementEntry.value().display().orElse(null);
        if (display == null) {
            return false;
        }
        if (advancement.requirements().requirements().size() <= 1) {
            return false;
        }
        if (display.isHidden()) {
            AdvancementProgress progress = advancementHandler.advancementProgresses.get(advancementEntry);
            return progress != null && progress.isDone();
        }
        return true;
    }

    @Unique
    private @Nullable AdvancementWidget getListAdvancementWidget(int index) {
        List<PlacedAdvancement> advancements = isCriteriaSearchActive
            ? criteriaSearchResults
            : getAdvancementsWithProgress();
        if (index < 0 || index >= advancements.size()) {
            return null;
        }
        PlacedAdvancement placedAdvancement = advancements.get(index);
        if (placedAdvancement == null || selectedTab == null || client == null) {
            return null;
        }
        AdvancementDisplay display = placedAdvancement.getAdvancement().display().orElse(null);
        if (display == null) {
            return null;
        }
        return advancementWidgetsCache.computeIfAbsent(placedAdvancement, k -> {
            AdvancementWidget widget = new AdvancementWidget(selectedTab, client, k, display);
            ((AdvancementWidgetExtension) widget).achievehunter$disableMirroring();
            widget.setProgress(advancementHandler.advancementProgresses.get(k.getAdvancementEntry()));
            return widget;
        });
    }

    @SuppressWarnings("SameParameterValue")
    @Unique
    private void renderDarkeningSection(DrawContext context, int x, int y, int width, int height) {
        if (client == null) {
            return;
        }
        int lineWidth = 32;
        int lineHeight = 2;

        Identifier headerTexture;
        Identifier backgroundTexture;
        Identifier footerTexture;
        if (client.world == null) {
            headerTexture = HEADER_SEPARATOR_TEXTURE;
            backgroundTexture = MENU_LIST_BACKGROUND_TEXTURE;
            footerTexture = FOOTER_SEPARATOR_TEXTURE;
        } else {
            headerTexture = INWORLD_HEADER_SEPARATOR_TEXTURE;
            backgroundTexture = INWORLD_MENU_LIST_BACKGROUND_TEXTURE;
            footerTexture = INWORLD_FOOTER_SEPARATOR_TEXTURE;
        }
        context.drawTexture(
            RenderLayer::getGuiTextured,
            headerTexture,
            x,
            y,
            0,
            0,
            width,
            lineHeight,
            lineWidth,
            lineHeight
        );
        context.drawTexture(
            RenderLayer::getGuiTextured,
            backgroundTexture,
            x,
            y + lineHeight,
            0,
            0,
            width,
            height - lineHeight * 2,
            lineWidth,
            lineWidth
        );
        context.drawTexture(
            RenderLayer::getGuiTextured,
            footerTexture,
            x,
            y + height - lineHeight,
            0,
            0,
            width,
            lineHeight,
            lineWidth,
            lineHeight
        );
    }

    @Unique
    private void searchCriteria() {
        String query = criteriaSearchField.getText().toLowerCase(Locale.ROOT);
        criteriaSearchResults.clear();
        for (PlacedAdvancement advancement : getAdvancementsWithProgress()) {
            AdvancementEntry advancementEntry = advancement.getAdvancementEntry();
            boolean found = false;
            for (AdvancementCriterionItem criterion : getCriteria(advancementEntry, false)) {
                if (criterion.rawName().contains(query)) {
                    criteriaSearchResults.add(advancement);
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            for (AdvancementCriterionItem criterion : getCriteria(advancementEntry, true)) {
                if (criterion.rawName().contains(query)) {
                    criteriaSearchResults.add(advancement);
                    break;
                }
            }
        }
        isCriteriaSearchActive = !query.trim().isEmpty();
        boolean isSearchResultsEmpty = criteriaSearchResults.isEmpty();
        if (isCriteriaSearchActive && !isSearchResultsEmpty) {
            setCurrentCriteriaSearchResultIndex(0);
            setSelectedAdvancement(criteriaSearchResults.getFirst().getAdvancementEntry());
        } else {
            currentCriteriaSearchResultIndex = -1;
            setCurrentAdvancementIndex(currentAdvancementIndex);
            if (isSearchResultsEmpty) {
                setSelectedAdvancement(null);
            }
        }
    }

    @Unique
    private int getAdvancementsListWidth() {
        int minCriteriaWidth = MIN_CRITERIA_LIST_WIDGET_WIDTH * 2 + CRITERIA_LIST_MARGIN * 3;
        int criteriaWidth = Math.max(minCriteriaWidth, width - MIN_ADVANCEMENTS_LIST_WIDTH);
        if (criteriaWidth > width / 3 * 2) {
            criteriaWidth = width / 3 * 2;
        }
        return width - criteriaWidth;
    }

    @Unique
    private void calculateWindowSizeAndPosition(int screenWidth, int screenHeight) {
        int tabSize = AdvancementTabType.ABOVE.width;

        int tabsHorizontalSpacing = AdvancementTabType.ABOVE.getTabX(1) - tabSize;
        int availableScreenWidth = screenWidth - Constants.ADVANCEMENTS_SCREEN_MINIMUM_MARGIN * 2;
        int maxWindowWidth = 0;
        int horizontalTabsCount = 1;
        while (true) {
            int requiredWidth = horizontalTabsCount * tabSize + (horizontalTabsCount - 1) * tabsHorizontalSpacing;
            if (requiredWidth > availableScreenWidth) {
                break;
            }
            maxWindowWidth = requiredWidth;
            horizontalTabsCount++;
        }
        windowWidth = maxWindowWidth;
        windowHorizontalMargin = (screenWidth - windowWidth) / 2;

        int availableScreenHeight = screenHeight - Constants.ADVANCEMENTS_SCREEN_MINIMUM_MARGIN * 2;
        int maxWindowHeight = 0;
        int verticalTabsCount = 1;
        while (true) {
            int requiredHeight = verticalTabsCount * tabSize;
            if (requiredHeight > availableScreenHeight) {
                break;
            }
            maxWindowHeight = requiredHeight;
            verticalTabsCount++;
        }
        windowHeight = maxWindowHeight;
        windowVerticalMargin = (screenHeight - windowHeight) / 2;
    }

    @Unique
    private @NotNull ArrayList<PlacedAdvancement> getAdvancements(boolean shouldExcludeRoots) {
        ArrayList<PlacedAdvancement> advancements = new ArrayList<>();
        AdvancementManager advancementManager = advancementHandler.getManager();
        Map<AdvancementEntry, AdvancementProgress> progresses = advancementHandler.advancementProgresses;
        for (AdvancementEntry advancementEntry : new ArrayList<>(progresses.keySet())) {
            if (advancementEntry == null) {
                continue;
            }
            Advancement advancement = advancementEntry.value();
            if (shouldExcludeRoots && advancement.isRoot()) {
                continue;
            }
            AdvancementDisplay display = advancementEntry.value().display().orElse(null);
            if (display == null) {
                continue;
            }
            if (display.isHidden()) {
                AdvancementProgress progress = progresses.get(advancementEntry);
                if (progress == null || !progress.isDone()) {
                    continue;
                }
            }
            PlacedAdvancement placedAdvancement = advancementManager.get(advancementEntry);
            if (placedAdvancement == null) {
                continue;
            }
            PlacedAdvancement rootAdvancement = placedAdvancement.getRoot();
            if (rootAdvancement == null) {
                continue;
            }
            advancements.add(placedAdvancement);
        }
        return advancements;
    }

    @Unique
    private void searchAdvancementsByUser() {
        if (advancementsSearchField == null) {
            return;
        }
        String query = advancementsSearchField.getText();
        isAdvancementsSearchActive = !query.isEmpty();
        searchAdvancementsInternal(SearchByType.getQueryWithoutMask(query), SearchByType.findByMask(query));
        showSearchResults();
    }

    @Unique
    private void searchAdvancementsInternal(String query, SearchByType searchByType) {
        query = query.toLowerCase(Locale.ROOT);
        searchResults.clear();
        if (query.trim().isEmpty()) {
            return;
        }
        boolean checkEverywhere = searchByType == SearchByType.EVERYWHERE;
        for (PlacedAdvancement placedAdvancement : getAdvancements(true)) {
            AdvancementDisplay display = placedAdvancement.getAdvancement().display().orElse(null);
            if (display == null) {
                continue;
            }
            String title = display.getTitle().getString().toLowerCase(Locale.ROOT);
            String description = display.getDescription().getString().toLowerCase(Locale.ROOT);
            String iconName = display.getIcon().getItem().getName().getString().toLowerCase(Locale.ROOT);

            if ((checkEverywhere || searchByType == SearchByType.TITLE) && title.contains(query) ||
                (checkEverywhere || searchByType == SearchByType.DESCRIPTION) && description.contains(query) ||
                (checkEverywhere || searchByType == SearchByType.ICON) && iconName.contains(query)
            ) {
                searchResults.add(placedAdvancement);
            }
        }
        searchResults.sort(
            Comparator
                .comparing((PlacedAdvancement placedAdvancement) -> {
                    AdvancementDisplay display = placedAdvancement.getAdvancement().display().orElse(null);
                    if (display == null) {
                        return Integer.MAX_VALUE;
                    }
                    return FRAME_TYPE_PRIORITY.indexOf(display.getFrame());
                })
                .thenComparing(advancement -> advancement.getAdvancementEntry().id())
        );
    }

    @Unique
    private void showSearchResults() {
        if (searchTab == null) {
            return;
        }
        resetSearchTab();
        if (searchResults.isEmpty()) {
            return;
        }
        searchTab.addWidget(searchTab.rootWidget, searchRootAdvancement.getAdvancementEntry());

        int rowIndex = 0;
        int columnIndex = 0;
        Map<AdvancementEntry, AdvancementProgress> progresses = advancementHandler.advancementProgresses;
        PlacedAdvancement rootAdvancement = new PlacedAdvancement(searchRootAdvancement.getAdvancementEntry(), null);
        PlacedAdvancement parentPlacedAdvancement = rootAdvancement;
        for (PlacedAdvancement searchResult : searchResults) {
            AdvancementDisplay searchResultDisplay = searchResult.getAdvancement().display().orElse(null);
            if (searchResultDisplay == null) {
                continue;
            }
            AdvancementDisplay searchResultAdvancementDisplay = new AdvancementDisplay(
                searchResultDisplay.getIcon(),
                searchResultDisplay.getTitle(),
                searchResultDisplay.getDescription(),
                searchResultDisplay.getBackground(),
                searchResultDisplay.getFrame(),
                searchResultDisplay.shouldShowToast(),
                searchResultDisplay.shouldAnnounceToChat(),
                searchResultDisplay.isHidden()
            );
            searchResultAdvancementDisplay.setPos(columnIndex, rowIndex);

            Advancement.Builder searchResultAdvancementBuilder = Advancement.Builder.create()
                .parent(parentPlacedAdvancement.getAdvancementEntry())
                .display(searchResultAdvancementDisplay)
                .rewards(searchResult.getAdvancement().rewards())
                .requirements(searchResult.getAdvancement().requirements());
            searchResult.getAdvancement().criteria().forEach(searchResultAdvancementBuilder::criterion);
            if (searchResult.getAdvancement().sendsTelemetryEvent()) {
                searchResultAdvancementBuilder = searchResultAdvancementBuilder.sendsTelemetryEvent();
            }
            AdvancementEntry searchResultAdvancementEntry =
                searchResultAdvancementBuilder.build(searchResult.getAdvancementEntry().id());
            PlacedAdvancement searchResultPlacedAdvancement =
                new PlacedAdvancement(searchResultAdvancementEntry, parentPlacedAdvancement);

            searchTab.addAdvancement(searchResultPlacedAdvancement);
            searchTab.widgets.get(searchResultAdvancementEntry)
                .setProgress(progresses.get(searchResultAdvancementEntry));
            if (columnIndex == advancementsSearchResultsColumnsCount - 1) {
                parentPlacedAdvancement = rootAdvancement;
                columnIndex = 0;
                rowIndex++;
            } else {
                parentPlacedAdvancement = new PlacedAdvancement(
                    searchResultAdvancementEntry,
                    searchResultPlacedAdvancement
                );
                columnIndex++;
            }
        }
    }

    @Unique
    private void resetSearchTab() {
        if (searchTab == null) {
            return;
        }
        searchTab.minPanX = Integer.MAX_VALUE;
        searchTab.minPanY = Integer.MAX_VALUE;
        searchTab.maxPanX = Integer.MIN_VALUE;
        searchTab.maxPanY = Integer.MIN_VALUE;
        searchTab.originX = advancementsSearchResultsOriginX;
        searchTab.originY = 0;
        searchTab.initialized = true;
        for (AdvancementWidget widget : searchTab.widgets.values()) {
            widget.parent = null;
            widget.children.clear();
        }
        searchTab.widgets.clear();
    }

    @Unique
    private void highlight(@NotNull PlacedAdvancement advancement, HighlightType type) {
        if (highlightedAdvancement != null) {
            return;
        }
        isAdvancementsSearchActive = false;
        highlightedAdvancement = advancement;
        highlightType = type;
        advancementHandler.selectTab(advancement.getRoot().getAdvancementEntry(), true);
    }

    @Override
    public void achievehunter$tick() {
        if (focusedAdvancementReleaseClickTimer > 0) {
            focusedAdvancementReleaseClickTimer--;
            if (focusedAdvancementReleaseClickTimer == 0) {
                isFocusedAdvancementClicked = false;
                focusedPlacedAdvancement = null;
                focusedAdvancementWidget = null;
            }
        }
        if (widgetHighlightCounter > 0) {
            widgetHighlightCounter--;
            if (widgetHighlightCounter == 0) {
                achievehunter$stopHighlight();
            }
        }
    }

    @Override
    public boolean achievehunter$charTyped(char chr, int modifiers) {
        if (isCriteriaOpened) {
            String oldText = criteriaSearchField.getText();
            if (criteriaSearchField.charTyped(chr, modifiers)) {
                if (!Objects.equals(oldText, criteriaSearchField.getText())) {
                    searchCriteria();
                }
                return true;
            }
        } else {
            String oldText = advancementsSearchField.getText();
            if (advancementsSearchField.charTyped(chr, modifiers)) {
                if (!Objects.equals(oldText, advancementsSearchField.getText())) {
                    searchAdvancementsByUser();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int achievehunter$getWindowWidth() {
        return windowWidth;
    }

    @Override
    public int achievehunter$getWindowHeight() {
        return windowHeight;
    }

    @Override
    public int achievehunter$getWindowHorizontalMargin() {
        return windowHorizontalMargin;
    }

    @Override
    public int achievehunter$getWindowVerticalMargin() {
        return windowVerticalMargin;
    }

    @Override
    public void achievehunter$resize(MinecraftClient client, int width, int height) {
        String advancementsSearchQuery = advancementsSearchField.getText();
        String criteriaSearchQuery = criteriaSearchField.getText();
        init(client, width, height);
        advancementsSearchField.setText(advancementsSearchQuery);
        criteriaSearchField.setText(criteriaSearchQuery);

        tabs.values().forEach((tab) -> tab.initialized = false);
        calculateWindowSizeAndPosition(width, height);
    }

    @Override
    public void achievehunter$setFocusedAdvancement(@Nullable AdvancementWidget advancementWidget) {
        PlacedAdvancement placedAdvancement = advancementWidget != null ? advancementWidget.advancement : null;
        if (isCriteriaOpened && Objects.equals(focusedPlacedAdvancement, placedAdvancement)) {
            return;
        }
        focusedPlacedAdvancement = placedAdvancement;
        focusedAdvancementWidget = advancementWidget;
    }

    @Override
    public void achievehunter$onMouseReleased(double mouseX, double mouseY, int button) {
        if (focusedPlacedAdvancement != null &&
            isFocusedAdvancementClicked &&
            button == GLFW.GLFW_MOUSE_BUTTON_LEFT
        ) {
            double deltaX = mouseX - focusedAdvancementClickX;
            double deltaY = mouseY - focusedAdvancementClickY;
            if (Math.sqrt(deltaX * deltaX + deltaY * deltaY) < DRAG_THRESHOLD) {
                if (focusedAdvancementWidget == null) {
                    return;
                }
                if (focusedAdvancementWidget.tab == searchTab) {
                    Identifier focusedAdvancementId = focusedAdvancementWidget.advancement.getAdvancementEntry().id();
                    for (PlacedAdvancement advancement : getAdvancements(true)) {
                        if (advancement.getAdvancementEntry().id().equals(focusedAdvancementId)) {
                            highlight(advancement, HighlightType.WIDGET);
                            break;
                        }
                    }
                } else {
                    MinecraftClient.getInstance().keyboard.setClipboard("\"" + focusedPlacedAdvancement.getAdvancementEntry().id() + "\", # " + focusedPlacedAdvancement.getAdvancement().display().orElseThrow().getDescription().getString());
                    if (shouldIncludeAdvancement(focusedPlacedAdvancement)) {
                        List<PlacedAdvancement> advancementsWithProgress = getAdvancementsWithProgress();
                        setCurrentAdvancementIndex(advancementsWithProgress.indexOf(focusedPlacedAdvancement));
                        setSelectedAdvancement(focusedPlacedAdvancement.getAdvancementEntry());
                        isCriteriaOpened = true;
                        advancementsSearchField.setFocusUnlocked(true);
                        advancementsSearchField.setFocused(false);
                        criteriaSearchField.setFocusUnlocked(false);
                        criteriaSearchField.setFocused(true);
                    }
                }
            }
            isFocusedAdvancementClicked = false;
        }
    }

    @Override
    public boolean achievehunter$isSearchActive() {
        return isAdvancementsSearchActive;
    }

    @Override
    public int achievehunter$getTreeWidth() {
        return windowWidth - (PAGE_OFFSET_X * 2);
    }

    @Override
    public int achievehunter$getTreeHeight() {
        return windowHeight - (PAGE_OFFSET_Y + PAGE_OFFSET_X);
    }

    @Override
    public Identifier achievehunter$getHighlightedAdvancementId() {
        return highlightedAdvancementId;
    }

    @Override
    public HighlightType achievehunter$getHighlightType() {
        return highlightType;
    }

    @Override
    public boolean achievehunter$isHighlightAtInvisibleState() {
        return widgetHighlightCounter != 0 && (widgetHighlightCounter / WIDGET_HIGHLIGHT_TICKS) % 2 == 0;
    }

    @Override
    public void achievehunter$stopHighlight() {
        highlightedAdvancementId = null;
        highlightType = null;
        widgetHighlightCounter = 0;
    }

    @Override
    public void achievehunter$searchAdvancements(
        String query,
        SearchByType searchByType,
        boolean autoHighlightSingle,
        HighlightType highlightType
    ) {
        searchAdvancementsInternal(query, searchByType);
        if (autoHighlightSingle && searchResults.size() == 1) {
            highlight(searchResults.getFirst(), highlightType);
            searchResults.clear();
            return;
        }
        query = SearchByType.addMaskToQuery(query, searchByType);
        advancementsSearchField.setText(query);
        isAdvancementsSearchActive = !query.isEmpty();
        showSearchResults();
    }

    @Override
    public void achievehunter$highlightAdvancement(Identifier advancementId, HighlightType highlightType) {
        for (PlacedAdvancement advancement : getAdvancements(false)) {
            if (advancementId.equals(advancement.getAdvancementEntry().id())) {
                highlight(advancement, highlightType);
                break;
            }
        }
    }

    @Shadow
    @Final
    public static int PAGE_OFFSET_X;

    @Shadow
    @Final
    public static int PAGE_OFFSET_Y;

    @Shadow
    @Final
    private static Text ADVANCEMENTS_TEXT;

    @Shadow
    @Final
    public static Identifier WINDOW_TEXTURE;

    @Shadow
    @Final
    private ThreePartsLayoutWidget layout;

    @Shadow
    @Final
    private ClientAdvancementManager advancementHandler;

    @Shadow
    @Final
    private Map<AdvancementEntry, AdvancementTab> tabs;

    @Shadow
    private @Nullable AdvancementTab selectedTab;

    @Shadow
    protected abstract void refreshWidgetPositions();

    @Shadow
    @Nullable
    public abstract AdvancementWidget getAdvancementWidget(PlacedAdvancement advancement);

    @Redirect(
        method = "drawWindow",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V",
            ordinal = 0
        )
    )
    public void drawFullscreenWindow(
        DrawContext context,
        Function<Identifier, RenderLayer> renderLayers,
        Identifier sprite,
        int x,
        int y,
        float u,
        float v,
        int width,
        int height,
        int textureWidth,
        int textureHeight
    ) {
        fullscreenAdvancementsWindow.draw(
            context,
            renderLayers,
            windowHorizontalMargin,
            windowVerticalMargin,
            windowWidth,
            windowHeight
        );
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = WINDOW_WIDTH,
            ordinal = 0
        )
    )
    private int calculateHalfOfScreenWidthOnRender(int originalValue) {
        return achievehunter$getWindowWidth();
    }

    @ModifyConstant(
        method = "render",
        constant = @Constant(
            intValue = WINDOW_HEIGHT,
            ordinal = 0
        )
    )
    private int calculateHalfOfScreenHeightOnRender(int originalValue) {
        return achievehunter$getWindowHeight();
    }

    @ModifyConstant(
        method = "mouseClicked",
        constant = @Constant(
            intValue = WINDOW_WIDTH,
            ordinal = 0
        )
    )
    private int calculateHalfOfScreenWidthOnMouseClicked(int originalValue) {
        return achievehunter$getWindowWidth();
    }

    @ModifyConstant(
        method = "mouseClicked",
        constant = @Constant(
            intValue = WINDOW_HEIGHT,
            ordinal = 0
        )
    )
    private int calculateHalfOfScreenHeightOnMouseClicked(int originalValue) {
        return achievehunter$getWindowHeight();
    }

    @ModifyConstant(
        method = "drawAdvancementTree",
        constant = @Constant(
            intValue = PAGE_WIDTH,
            ordinal = 0
        )
    )
    private int calculateWidthOfEmptyBlackBackground(int originalValue) {
        return achievehunter$getTreeWidth();
    }

    @ModifyConstant(
        method = "drawAdvancementTree",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 0
        )
    )
    private int calculateHeightOfEmptyBlackBackground(int originalValue) {
        return achievehunter$getTreeHeight();
    }

    @ModifyConstant(
        method = "drawAdvancementTree",
        constant = @Constant(
            intValue = PAGE_WIDTH / 2,
            ordinal = 0
        )
    )
    private int moveEmptyTextAndSadLabelTextToCenterOfWidth(int originalValue) {
        return achievehunter$getTreeWidth() / 2;
    }

    @ModifyConstant(
        method = "drawAdvancementTree",
        constant = @Constant(
            intValue = PAGE_HEIGHT / 2,
            ordinal = 0
        )
    )
    private int moveEmptyTextToCenterOfHeight(int originalValue) {
        return achievehunter$getTreeHeight() / 2;
    }

    @ModifyConstant(
        method = "drawAdvancementTree",
        constant = @Constant(
            intValue = PAGE_HEIGHT,
            ordinal = 1
        )
    )
    private int moveSadLabelTextToBottom(int originalValue) {
        return achievehunter$getTreeHeight();
    }

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addHeader(Lnet/minecraft/text/Text;Lnet/minecraft/client/font/TextRenderer;)V",
            ordinal = 0
        )
    )
    private void removeHeader(ThreePartsLayoutWidget layout, Text text, TextRenderer textRenderer) {
    }

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;addFooter(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;",
            ordinal = 0
        )
    )
    private <T extends Widget> @Nullable T removeFooter(ThreePartsLayoutWidget layout, T widget) {
        return null;
    }

    @Redirect(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/widget/ThreePartsLayoutWidget;forEachChild(Ljava/util/function/Consumer;)V",
            ordinal = 0
        )
    )
    private void cancelAddDrawableChild(ThreePartsLayoutWidget layout, Consumer<ClickableWidget> consumer) {
    }

    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;refreshWidgetPositions()V",
            shift = At.Shift.BEFORE
        )
    )
    public void initInject(CallbackInfo ci) {
        if (searchTab == null) {
            AdvancementDisplay searchRootAdvancementDisplay = new AdvancementDisplay(
                ItemStack.EMPTY,
                Text.empty(),
                Text.empty(),
                Optional.empty(),
                AdvancementFrame.TASK,
                false,
                false,
                true
            );
            searchRootAdvancement = new PlacedAdvancement(
                Advancement.Builder
                    .createUntelemetered()
                    .display(searchRootAdvancementDisplay)
                    .build(AchieveHunterMod.ADVANCEMENTS_SEARCH_ID),
                null
            );
            AdvancementsScreen advancementsScreen = (AdvancementsScreen) (Object) this;
            if (client != null) {
                searchTab = new AdvancementTab(
                    client,
                    advancementsScreen,
                    null,
                    0,
                    searchRootAdvancement,
                    searchRootAdvancementDisplay
                );
            }
        }
        advancementsSearchField = new TextFieldWidget(
            textRenderer,
            0,
            0,
            SEARCH_FIELD_RECT.width - SEARCH_FIELD_TEXT_LEFT_OFFSET - 8,
            textRenderer.fontHeight,
            ScreenTexts.EMPTY
        );
        advancementsSearchField.setDrawsBackground(false);
        advancementsSearchField.setEditableColor(Colors.WHITE);
        advancementsSearchField.setFocusUnlocked(false);
        advancementsSearchField.setFocused(true);
        addSelectableChild(advancementsSearchField);

        criteriaSearchField = new TextFieldWidget(
            textRenderer,
            0,
            0,
            SEARCH_FIELD_RECT.width - SEARCH_FIELD_TEXT_LEFT_OFFSET - 8,
            textRenderer.fontHeight,
            ScreenTexts.EMPTY
        );
        criteriaSearchField.setDrawsBackground(false);
        criteriaSearchField.setEditableColor(Colors.WHITE);
        criteriaSearchField.setFocusUnlocked(false);
        addSelectableChild(criteriaSearchField);

        unobtainedCriteriaListWidget = new AdvancementCriteriaListWidget(
            client,
            false,
            CRITERIA_ITEM_HEIGHT,
            CRITERIA_HEADER_HEIGHT
        );
        addDrawableChild(unobtainedCriteriaListWidget);

        obtainedCriteriaListWidget = new AdvancementCriteriaListWidget(
            client,
            true,
            CRITERIA_ITEM_HEIGHT,
            CRITERIA_HEADER_HEIGHT
        );
        addDrawableChild(obtainedCriteriaListWidget);

        calculateWindowSizeAndPosition(width, height);
    }

    @Inject(
        method = "refreshWidgetPositions",
        at = @At(value = "TAIL")
    )
    public void onRefreshWidgetPositions(CallbackInfo ci) {
        setSelectedAdvancement(selectedAdvancement);
    }

    @Inject(
        method = "mouseClicked",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void onMouseClicked(
        double mouseX,
        double mouseY,
        int button,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isCriteriaOpened) {
            if (criteriaSearchField.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
            if (unobtainedCriteriaListWidget.isMouseOver(mouseX, mouseY)) {
                unobtainedCriteriaListWidget.mouseClicked(mouseX, mouseY, button);
                cir.setReturnValue(true);
                return;
            }
            if (obtainedCriteriaListWidget.isMouseOver(mouseX, mouseY)) {
                obtainedCriteriaListWidget.mouseClicked(mouseX, mouseY, button);
                cir.setReturnValue(true);
                return;
            }
            cir.setReturnValue(true);
        } else {
            if (advancementsSearchField.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
            if (focusedPlacedAdvancement != null &&
                !isFocusedAdvancementClicked &&
                button == GLFW.GLFW_MOUSE_BUTTON_LEFT
            ) {
                focusedAdvancementClickX = mouseX;
                focusedAdvancementClickY = mouseY;
                focusedAdvancementReleaseClickTimer = FOCUSED_ADVANCEMENT_CLICK_TIMEOUT_TICKS;
                isFocusedAdvancementClicked = true;
            } else {
                isFocusedAdvancementClicked = false;
            }
        }
    }

    @Inject(
        method = "mouseDragged",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void onMouseDragged(
        double mouseX,
        double mouseY,
        int button,
        double deltaX,
        double deltaY,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isCriteriaOpened) {
            if (unobtainedCriteriaListWidget.isMouseOver(mouseX, mouseY)) {
                unobtainedCriteriaListWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                cir.setReturnValue(true);
                return;
            }
            if (obtainedCriteriaListWidget.isMouseOver(mouseX, mouseY)) {
                obtainedCriteriaListWidget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(
        method = "mouseScrolled",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void onMouseScrolled(
        double mouseX,
        double mouseY,
        double horizontalAmount,
        double verticalAmount,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isCriteriaOpened) {
            if (horizontalAmount == 0 && verticalAmount != 0 && mouseX >= 0 && mouseX <= getAdvancementsListWidth()) {
                if (isCriteriaSearchActive) {
                    setCurrentCriteriaSearchResultIndex(currentCriteriaSearchResultIndex - (int) verticalAmount);
                } else {
                    setCurrentAdvancementIndex(currentAdvancementIndex - (int) verticalAmount);
                }
                cir.setReturnValue(true);
                return;
            }
            if (unobtainedCriteriaListWidget.isMouseOver(mouseX, mouseY)) {
                unobtainedCriteriaListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                cir.setReturnValue(true);
                return;
            }
            if (obtainedCriteriaListWidget.isMouseOver(mouseX, mouseY)) {
                obtainedCriteriaListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    public void renderInfo(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (client == null) {
            return;
        }
        if (isCriteriaOpened) {
            int advancementIndex = isCriteriaSearchActive ? currentCriteriaSearchResultIndex : currentAdvancementIndex;
            AdvancementWidget centerWidget = getListAdvancementWidget(advancementIndex);
            if (centerWidget instanceof AdvancementWidgetExtension centerAdvancementWidgetExtension) {
                int criteriaSectionWidth = obtainedCriteriaListWidget.getWidth();
                int criteriaSectionHeight = obtainedCriteriaListWidget.getHeight();
                int criteriaSectionTop = obtainedCriteriaListWidget.getY();

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 0);

                int listWidth = getAdvancementsListWidth();
                listWidth--;
                listWidth--;
                int listHeight = height - PAGE_OFFSET_Y - PAGE_OFFSET_X;
                int listCenterX = listWidth / 2;
                int listCenterY = PAGE_OFFSET_Y + listHeight / 2;

                int listSpacing = 10;
                int listCenterExtraSpacing = 8;

                centerAdvancementWidgetExtension.achievehunter$setCollapsed(false);

                int centerTooltipWidth = centerAdvancementWidgetExtension.achievehunter$getTooltipWidth();
                int centerTooltipHeight = centerAdvancementWidgetExtension.achievehunter$getTooltipHeight(true);
                int centerTooltipX = listCenterX - centerTooltipWidth / 2;
                int centerTooltipY = listCenterY - centerTooltipHeight / 2;

                centerAdvancementWidgetExtension.achievehunter$setX(centerTooltipX);
                centerAdvancementWidgetExtension.achievehunter$setY(centerTooltipY);

                int prevDarkeningSectionBottom = centerTooltipY
                    - listCenterExtraSpacing;
                int nextDarkeningSectionTop = centerTooltipY
                    + centerTooltipHeight
                    + listCenterExtraSpacing;
                int darkeningHeight = centerTooltipHeight + listCenterExtraSpacing * 2;
                renderDarkeningSection(
                    context,
                    0,
                    0,
                    listWidth,
                    prevDarkeningSectionBottom
                );
                renderDarkeningSection(
                    context,
                    0,
                    nextDarkeningSectionTop,
                    listWidth,
                    height - nextDarkeningSectionTop
                );

                int grayLineColor = 0x33ffffff;
                int blackLineColor = 0xbf000000;
                context.drawVerticalLine(listWidth, -1, prevDarkeningSectionBottom - 1, blackLineColor);
                context.drawVerticalLine(listWidth + 1, -1, prevDarkeningSectionBottom - 1, grayLineColor);
                context.drawHorizontalLine(listWidth, listWidth + 1, prevDarkeningSectionBottom - 1, grayLineColor);

                context.drawVerticalLine(listWidth, nextDarkeningSectionTop, height, blackLineColor);
                context.drawVerticalLine(listWidth + 1, nextDarkeningSectionTop, height, grayLineColor);
                context.drawHorizontalLine(listWidth, listWidth + 1, nextDarkeningSectionTop, grayLineColor);

                context.getMatrices().pop();
                centerWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);

                int nextIndex = advancementIndex;
                float nextTooltipTop = centerTooltipY + centerTooltipHeight + listSpacing + listCenterExtraSpacing;
                while (nextTooltipTop <= height - PAGE_OFFSET_X) {
                    nextIndex++;
                    AdvancementWidget nextWidget = getListAdvancementWidget(nextIndex);
                    if (!(nextWidget instanceof AdvancementWidgetExtension nextAdvancementWidgetExtension)) {
                        break;
                    }
                    nextAdvancementWidgetExtension.achievehunter$setCollapsed(true);
                    int nextTooltipWidth = nextAdvancementWidgetExtension.achievehunter$getTooltipWidth();
                    int nextTooltipHeight = nextAdvancementWidgetExtension.achievehunter$getTooltipHeight(false);
                    int nextTooltipCenterX = nextTooltipWidth / 2;
                    int nextTooltipX = listCenterX - nextTooltipCenterX;

                    nextAdvancementWidgetExtension.achievehunter$setX(nextTooltipX);
                    nextAdvancementWidgetExtension.achievehunter$setY(MathHelper.floor(nextTooltipTop));

                    nextWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
                    nextTooltipTop += nextTooltipHeight + listSpacing;
                }

                int prevIndex = advancementIndex;
                float prevTooltipBottom = centerTooltipY - listSpacing - listCenterExtraSpacing;
                while (prevTooltipBottom >= PAGE_OFFSET_Y) {
                    prevIndex--;
                    AdvancementWidget prevWidget = getListAdvancementWidget(prevIndex);
                    if (!(prevWidget instanceof AdvancementWidgetExtension prevAdvancementWidgetExtension)) {
                        break;
                    }
                    prevAdvancementWidgetExtension.achievehunter$setCollapsed(true);
                    int prevTooltipWidth = prevAdvancementWidgetExtension.achievehunter$getTooltipWidth();
                    int prevTooltipHeight = prevAdvancementWidgetExtension.achievehunter$getTooltipHeight(false);
                    int prevTooltipCenterX = prevTooltipWidth / 2;
                    int prevTooltipX = listCenterX - prevTooltipCenterX;
                    float prevTooltipY = prevTooltipBottom - prevTooltipHeight;

                    prevAdvancementWidgetExtension.achievehunter$setX(prevTooltipX);
                    prevAdvancementWidgetExtension.achievehunter$setY(MathHelper.floor(prevTooltipY));

                    prevWidget.drawTooltip(context, 0, 0, 1.0f, 0, 0);
                    prevTooltipBottom -= prevTooltipHeight + listSpacing;
                }
            }
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 1000);
            int shadowHeight = 6;
            int windowTextureSize = 256;
            int textureRegionLeft = PAGE_OFFSET_X + shadowHeight;
            int textureRegionWidth = PAGE_WIDTH - textureRegionLeft;
            int textureRegionHeaderHeight = PAGE_OFFSET_Y + shadowHeight;
            int textureRegionHeaderTop = 0;
            int textureRegionFooterHeight = PAGE_OFFSET_X + shadowHeight;
            int textureRegionFooterTop = WINDOW_HEIGHT - textureRegionFooterHeight;
            context.drawTexture(
                RenderLayer::getGuiTextured,
                WINDOW_TEXTURE,
                0,
                0,
                textureRegionLeft,
                textureRegionHeaderTop,
                width,
                textureRegionHeaderHeight,
                textureRegionWidth,
                textureRegionHeaderHeight,
                windowTextureSize,
                windowTextureSize
            );

            Text title;
            if (isCriteriaSearchActive) {
                title = SEARCH_TITLE.copy()
                    .append(" (" + criteriaSearchResults.size() + ")");
            } else {
                title = ADVANCEMENTS_TEXT.copy()
                    .append(" (" + obtainedAdvancementsCount + "/" + totalAdvancementsCount + ")");
            }
            context.drawText(textRenderer, title, 6, 6, 4210752, false);

            int criteriaSearchFieldX = width - SEARCH_FIELD_RECT.width - 3;
            int criteriaSearchFieldY = (PAGE_OFFSET_Y - SEARCH_FIELD_RECT.height) / 2 + 1;
            context.drawTexture(
                RenderLayer::getGuiTextured,
                ItemGroups.ITEM_SEARCH_TAB_TEXTURE_ID,
                criteriaSearchFieldX,
                criteriaSearchFieldY,
                SEARCH_FIELD_RECT.x,
                SEARCH_FIELD_RECT.y,
                SEARCH_FIELD_RECT.width,
                SEARCH_FIELD_RECT.height,
                256,
                256
            );
            criteriaSearchField.setX(criteriaSearchFieldX + SEARCH_FIELD_TEXT_LEFT_OFFSET);
            criteriaSearchField.setY(criteriaSearchFieldY + SEARCH_FIELD_TEXT_LEFT_OFFSET);
            criteriaSearchField.render(context, mouseX, mouseY, delta);

            context.drawTexture(
                RenderLayer::getGuiTextured,
                WINDOW_TEXTURE,
                0,
                height - PAGE_OFFSET_X - shadowHeight,
                textureRegionLeft,
                textureRegionFooterTop,
                width,
                textureRegionFooterHeight,
                textureRegionWidth,
                textureRegionFooterHeight,
                windowTextureSize,
                windowTextureSize
            );
            context.getMatrices().pop();
            ci.cancel();
        }
    }

    @Inject(
        method = "keyPressed",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void ignoreKeyPressed(
        int keyCode,
        int scanCode,
        int modifiers,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (isCriteriaOpened) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                isCriteriaOpened = false;
                criteriaSearchField.setText("");
                criteriaSearchField.setFocusUnlocked(true);
                criteriaSearchField.setFocused(false);
                advancementsSearchField.setFocusUnlocked(false);
                advancementsSearchField.setFocused(true);
                currentCriteriaSearchResultIndex = -1;
                isCriteriaSearchActive = false;
                setSelectedAdvancement(null);
                cir.setReturnValue(true);
                return;
            }
            String oldText = criteriaSearchField.getText();
            if (criteriaSearchField.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(oldText, criteriaSearchField.getText())) {
                    searchCriteria();
                }
                cir.setReturnValue(true);
            }
        } else {
            String oldText = advancementsSearchField.getText();
            if (advancementsSearchField.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(oldText, advancementsSearchField.getText())) {
                    searchAdvancementsByUser();
                }
                cir.setReturnValue(true);
            }
            if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(
        method = "drawAdvancementTree",
        at = @At("TAIL")
    )
    private void startHighlight(DrawContext context, int mouseX, int mouseY, int x, int y, CallbackInfo ci) {
        if (highlightedAdvancement == null || selectedTab == null) {
            return;
        }
        for (AdvancementWidget widget : selectedTab.widgets.values()) {
            if (widget != null && widget.advancement == highlightedAdvancement) {
                int centerX = (WIDGET_SIZE - achievehunter$getTreeWidth()) / 2;
                int centerY = (WIDGET_SIZE - achievehunter$getTreeHeight()) / 2;
                selectedTab.move(
                    -(selectedTab.originX + widget.getX() + TREE_X_OFFSET + centerX),
                    -(selectedTab.originY + widget.getY() + centerY)
                );
                highlightedAdvancement = null;
                highlightedAdvancementId = widget.advancement.getAdvancementEntry().id();
                widgetHighlightCounter = WIDGET_HIGHLIGHT_COUNT * 2 * WIDGET_HIGHLIGHT_TICKS;
                break;
            }
        }
    }

    @Redirect(
        method = "mouseClicked",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientAdvancementManager;selectTab(Lnet/minecraft/advancement/AdvancementEntry;Z)V"
        )
    )
    private void mouseClickedRedirect(
        @NotNull ClientAdvancementManager advancementHandler,
        AdvancementEntry tab,
        boolean local
    ) {
        isAdvancementsSearchActive = false;
        achievehunter$stopHighlight();
        advancementHandler.selectTab(tab, true);
    }

    @Redirect(
        method = "drawAdvancementTree",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;selectedTab:Lnet/minecraft/client/gui/screen/advancement/AdvancementTab;",
            opcode = Opcodes.GETFIELD
        )
    )
    private @Nullable AdvancementTab drawAdvancementTreeInject(AdvancementsScreen screen) {
        return !isAdvancementsSearchActive ? selectedTab : searchTab.widgets.size() > 1 ? searchTab : null;
    }

    @ModifyArgs(
        method = "drawWindow",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementTab;drawBackground(Lnet/minecraft/client/gui/DrawContext;IIZ)V"
        )
    )
    private void drawWindowModifyTabSelected(Args args) {
        if (isAdvancementsSearchActive) {
            args.set(3, false);
        }
    }

    @Redirect(
        method = "drawWindow",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"
        )
    )
    private int modifyWindowTitleRender(
        DrawContext context,
        TextRenderer textRenderer,
        Text text,
        int x,
        int y,
        int color,
        boolean shadow
    ) {
        if (isAdvancementsSearchActive) {
            text = SEARCH_TITLE;
        }
        int rightEdgeX = x + achievehunter$getTreeWidth() - SEARCH_FIELD_RECT.width - 3;
        int textWidth = textRenderer.getWidth(text);
        int availableWidth = rightEdgeX - x;

        if (textWidth > availableWidth) {
            int bottomY = y + textRenderer.fontHeight;

            int excessWidth = textWidth - availableWidth;
            double timeInSeconds = TimeUnit.MILLISECONDS.toSeconds(Util.getMeasuringTimeMs());
            double adjustmentFactor = Math.max((double) excessWidth * 0.5, 3);
            double oscillation =
                Math.sin(Math.PI / 2 * Math.cos(Math.PI * 2 * timeInSeconds / adjustmentFactor)) / 2 + 0.5;
            double offset = MathHelper.lerp(oscillation, 0, excessWidth);

            context.enableScissor(x, y, rightEdgeX, bottomY);
            context.drawText(textRenderer, text, x - (int) offset, y, color, shadow);
            context.disableScissor();
        } else {
            context.drawText(textRenderer, text, x, y, color, shadow);
        }
        return 0;
    }

    @Redirect(
        method = "drawWidgetTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementTab;drawWidgetTooltip(Lnet/minecraft/client/gui/DrawContext;IIII)V"
        )
    )
    private void drawWidgetTooltipRedirectTab(
        AdvancementTab selectedTab,
        DrawContext context,
        int mouseX,
        int mouseY,
        int x,
        int y
    ) {
        if (isAdvancementsSearchActive) {
            selectedTab = searchTab;
        }
        selectedTab.drawWidgetTooltip(context, mouseX, mouseY, x, y);
    }

    @Redirect(
        method = "mouseScrolled",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;selectedTab:Lnet/minecraft/client/gui/screen/advancement/AdvancementTab;",
            opcode = Opcodes.GETFIELD
        )
    )
    private AdvancementTab mouseScrolledRedirect(AdvancementsScreen screen) {
        return isAdvancementsSearchActive ? searchTab : selectedTab;
    }

    @Redirect(
        method = "mouseDragged",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;selectedTab:Lnet/minecraft/client/gui/screen/advancement/AdvancementTab;",
            opcode = Opcodes.GETFIELD
        )
    )
    private AdvancementTab mouseDraggedRedirect(AdvancementsScreen screen) {
        return isAdvancementsSearchActive ? searchTab : selectedTab;
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;drawAdvancementTree(Lnet/minecraft/client/gui/DrawContext;IIII)V"
        )
    )
    public void getWindowSizes(
        AdvancementsScreen screen,
        DrawContext context,
        int mouseX,
        int mouseY,
        int x,
        int y,
        @NotNull Operation<Void> original
    ) {
        windowX = x;
        windowY = y;
        original.call(screen, context, mouseX, mouseY, x, y);
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screen/advancement/AdvancementsScreen;drawWindow(Lnet/minecraft/client/gui/DrawContext;II)V",
            shift = At.Shift.AFTER
        )
    )
    public void renderInject(
        DrawContext context,
        int mouseX,
        int mouseY,
        float delta,
        CallbackInfo ci
    ) {
        if (advancementsSearchField != null) {
            int frameOffset = 1;
            int frameContainerWidth = frameOffset + WIDGET_SIZE + frameOffset;
            int treeWidth = achievehunter$getTreeWidth();
            int columnsCount = treeWidth / frameContainerWidth;
            int rowWidth = frameContainerWidth * columnsCount;
            int horizontalOffset = treeWidth - rowWidth - TREE_X_OFFSET;
            int originX = horizontalOffset / 2;
            if (advancementsSearchResultsColumnsCount != columnsCount || advancementsSearchResultsOriginX != originX) {
                advancementsSearchResultsColumnsCount = columnsCount;
                advancementsSearchResultsOriginX = originX;
                if (isAdvancementsSearchActive) {
                    showSearchResults();
                }
            }

            int symmetryFixX = 1;
            int fieldX = windowX + PAGE_OFFSET_X + achievehunter$getTreeWidth() - SEARCH_FIELD_RECT.width + symmetryFixX;
            int fieldY = windowY + 4;

            context.drawTexture(
                RenderLayer::getGuiTextured,
                ItemGroups.ITEM_SEARCH_TAB_TEXTURE_ID,
                fieldX,
                fieldY,
                SEARCH_FIELD_RECT.x,
                SEARCH_FIELD_RECT.y,
                SEARCH_FIELD_RECT.width,
                SEARCH_FIELD_RECT.height,
                256,
                256
            );

            advancementsSearchField.setX(fieldX + SEARCH_FIELD_TEXT_LEFT_OFFSET);
            advancementsSearchField.setY(fieldY + SEARCH_FIELD_TEXT_LEFT_OFFSET);
            advancementsSearchField.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(
        method = "keyPressed",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void keyPressedInject(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (isCriteriaOpened) {
            String oldText = criteriaSearchField.getText();
            if (criteriaSearchField.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(oldText, criteriaSearchField.getText())) {
                    searchCriteria();
                }
                cir.setReturnValue(true);
                return;
            }
        } else {
            String oldText = advancementsSearchField.getText();
            if (advancementsSearchField.keyPressed(keyCode, scanCode, modifiers)) {
                if (!Objects.equals(oldText, advancementsSearchField.getText())) {
                    searchAdvancementsByUser();
                }
                cir.setReturnValue(true);
                return;
            }
        }
        if (keyCode != GLFW.GLFW_KEY_ESCAPE) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "mouseClicked",
        at = @At(value = "HEAD"),
        cancellable = true
    )
    public void mouseClickedInject(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (advancementsSearchField != null && advancementsSearchField.mouseClicked(mouseX, mouseY, button)) {
            isAdvancementsSearchActive = !advancementsSearchField.getText().isEmpty();
            cir.setReturnValue(true);
            return;
        }
        if (criteriaSearchField != null && criteriaSearchField.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            isCriteriaSearchActive = !criteriaSearchField.getText().isEmpty();
            return;
        }
        isFocusedAdvancementClicked = focusedAdvancementWidget != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }
}
