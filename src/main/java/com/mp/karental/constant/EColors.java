package com.mp.karental.constant;
/**
 * Represents the colors that a car could have
 * @author QuangPM20
 *
 * @version 1.0
 */
public enum EColors {
    WHITE("White"),
    BLACK("Black"),
    GRAY("Gray"),
    SILVER("Silver"),
    RED("Red"),
    BLUE("Blue"),
    BROWN("Brown"),
    GREEN("Green"),
    BEIGE("Beige"),
    GOLD("Gold"),
    YELLOW("Yellow"),
    PURPLE("Purple");

    private final String displayName;

    EColors(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

