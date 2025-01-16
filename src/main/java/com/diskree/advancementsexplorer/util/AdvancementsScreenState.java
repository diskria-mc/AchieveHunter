package com.diskree.advancementsexplorer.util;

public enum AdvancementsScreenState {

    WINDOW_VISIBLE,
    TRANSITION_TO_CAROUSEL,
    CAROUSEL_VISIBLE,
    TRANSITION_TO_WINDOW;

    public boolean isTransitionState() {
        return this == TRANSITION_TO_CAROUSEL || this == TRANSITION_TO_WINDOW;
    }
}
