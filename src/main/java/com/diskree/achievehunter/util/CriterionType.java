package com.diskree.achievehunter.util;

public enum CriterionType {

    ITEM("item.%namespace%.%criterion%"),
    BLOCK("block.%namespace%.%criterion%"),
    ENTITY("entity.%namespace%.%criterion%"),
    VILLAGER_PROFESSION("entity.%namespace%.villager.%criterion%"),
    ENCHANTMENT("enchantment.%namespace%.%criterion%"),
    EFFECT("effect.%namespace%.%criterion%"),
    BIOME("biome.%namespace%.%criterion%"),
    STRUCTURE("structure.%namespace%.%criterion%"),
    COLOR("color.%namespace%.%criterion%"),
    GOAT_HORN("instrument.%namespace%.%criterion%"),
    STAT("stat.%namespace%.%criterion%"),
    TRIM_PATTERN("trim_pattern.%namespace%.%criterion%"),
    PAINTING("painting.%namespace%.%criterion%.title");

    private final String localeFormat;

    CriterionType(String localeFormat) {
        this.localeFormat = localeFormat;
    }

    public String getLocaleFormat() {
        return localeFormat;
    }
}
