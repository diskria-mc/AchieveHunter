package com.diskree.advancementsexplorer.util;

public enum AdvancementsScreenState {

    WINDOW_VISIBLE,
    OPENING_INFO,
    INFO_VISIBLE,
    CLOSING_INFO;

    public boolean isTransitionState() {
        return this == OPENING_INFO || this == CLOSING_INFO;
    }
}
