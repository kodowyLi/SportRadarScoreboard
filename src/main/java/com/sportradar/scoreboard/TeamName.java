package com.sportradar.scoreboard;

import java.util.Locale;
import java.util.regex.Pattern;

// The only place where team names are validated and normalised.
record TeamName(String display, String key) {

    // \s alone misses no-break spaces (U+00A0, U+2007, U+202F); \p{Z} covers them.
    private static final Pattern WHITESPACE = Pattern.compile("[\\s\\p{Z}]+");

    static TeamName of(String rawName) {
        if (rawName == null) {
            throw new IllegalArgumentException("Team name must not be null or blank");
        }
        String display = WHITESPACE.matcher(rawName).replaceAll(" ").strip();
        if (display.isEmpty()) {
            throw new IllegalArgumentException("Team name must not be null or blank");
        }
        return new TeamName(display, display.toLowerCase(Locale.ROOT));
    }

    boolean sameTeamAs(TeamName other) {
        return key.equals(other.key);
    }
}
